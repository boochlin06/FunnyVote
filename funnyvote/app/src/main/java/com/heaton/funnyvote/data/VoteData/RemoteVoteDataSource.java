package com.heaton.funnyvote.data.VoteData;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RemoteVoteDataSource implements VoteDataSource {
    private static final String TAG = RemoteVoteDataSource.class.getSimpleName();
    private static RemoteVoteDataSource INSTANCE = null;

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private boolean hasCheckedSeed = false;

    public static RemoteVoteDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (RemoteVoteDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteVoteDataSource();
                }
            }
        }
        return INSTANCE;
    }

    public RemoteVoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    private String getUid(User user) {
        if (user != null && !TextUtils.isEmpty(user.getUserCode())) {
            return user.getUserCode();
        }
        if (auth.getCurrentUser() != null && !TextUtils.isEmpty(auth.getCurrentUser().getUid())) {
            return auth.getCurrentUser().getUid();
        }
        return "guest_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static VoteData mapDocToVoteData(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }
        VoteData voteData = new VoteData();
        String pollId = doc.getString("pollId");
        if (pollId == null) {
            pollId = doc.getId();
        }
        voteData.setVoteCode(pollId);
        voteData.setTitle(doc.getString("title"));
        voteData.setAuthorCode(doc.getString("authorId"));
        voteData.setAuthorName(doc.getString("authorName") != null ? doc.getString("authorName") : "匿名");
        voteData.setAuthorIcon(doc.getString("authorIcon"));
        voteData.setVoteImage(doc.getString("imageUrl"));
        voteData.setCategory(doc.getString("category"));
        voteData.setSecurity(doc.getString("security") != null ? doc.getString("security") : VoteData.SECURITY_PUBLIC);

        Boolean isNeedPw = doc.getBoolean("isNeedPassword");
        voteData.setIsNeedPassword(Boolean.TRUE.equals(isNeedPw));

        Boolean isPreview = doc.getBoolean("isCanPreviewResult");
        voteData.setIsCanPreviewResult(isPreview == null || isPreview);

        Boolean isAddOpt = doc.getBoolean("isUserCanAddOption");
        voteData.setIsUserCanAddOption(Boolean.TRUE.equals(isAddOpt));

        Long minOpt = doc.getLong("minOption");
        voteData.setMinOption(minOpt != null ? minOpt.intValue() : 1);

        Long maxOpt = doc.getLong("maxOption");
        voteData.setMaxOption(maxOpt != null ? maxOpt.intValue() : 1);

        Long optCount = doc.getLong("optionCount");
        voteData.setOptionCount(optCount != null ? optCount.intValue() : 0);

        Long totalVotes = doc.getLong("totalVotes");
        voteData.setPollCount(totalVotes != null ? totalVotes.intValue() : 0);

        Long startT = doc.getLong("startTime");
        Long createT = doc.getLong("createdAt");
        long now = System.currentTimeMillis();
        voteData.setStartTime(startT != null ? startT : (createT != null ? createT : now));

        Long endT = doc.getLong("endTime");
        voteData.setEndTime(endT != null ? endT : (voteData.getStartTime() + 86400000L * 30L));

        // Parse topOptions
        List<Map<String, Object>> topOptionsRaw = (List<Map<String, Object>>) doc.get("topOptions");
        if (topOptionsRaw != null && !topOptionsRaw.isEmpty()) {
            List<Option> parsedOptions = new ArrayList<>();
            for (int i = 0; i < topOptionsRaw.size(); i++) {
                Map<String, Object> map = topOptionsRaw.get(i);
                Option opt = new Option();
                opt.setVoteCode(pollId);
                opt.setCode((String) map.get("optionId"));
                opt.setTitle((String) map.get("title"));
                Number cnt = (Number) map.get("voteCount");
                opt.setCount(cnt != null ? cnt.intValue() : 0);
                parsedOptions.add(opt);

                if (i == 0) {
                    voteData.setFirstOption(opt);
                    voteData.setOption1Title(opt.getTitle());
                    voteData.setOption1Code(opt.getCode());
                    voteData.setOption1Count(opt.getCount());
                } else if (i == 1) {
                    voteData.setSecondOption(opt);
                    voteData.setOption2Title(opt.getTitle());
                    voteData.setOption2Code(opt.getCode());
                    voteData.setOption2Count(opt.getCount());
                }
            }
            Option top = null;
            for (Option opt : parsedOptions) {
                if (top == null || opt.getCount() > top.getCount()) {
                    top = opt;
                }
            }
            if (top != null) {
                voteData.setTopOption(top);
                voteData.setOptionTopCode(top.getCode());
                voteData.setOptionTopTitle(top.getTitle());
                voteData.setOptionTopCount(top.getCount());
            }
        }

        return voteData;
    }

    private void seedInitialDataIfEmpty(final Runnable onDone) {
        if (hasCheckedSeed) {
            if (onDone != null) onDone.run();
            return;
        }
        hasCheckedSeed = true;

        firestore.collection("polls").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && (task.getResult() == null || task.getResult().isEmpty())) {
                Log.d(TAG, "Firestore polls collection is empty. Seeding initial data...");
                WriteBatch batch = firestore.batch();
                long now = System.currentTimeMillis();

                // Poll 1
                String p1Id = "vote_001";
                DocumentReference p1Ref = firestore.collection("polls").document(p1Id);
                Map<String, Object> p1 = new HashMap<>();
                p1.put("pollId", p1Id);
                p1.put("title", "2026 年 Android 開發最推薦的架構是什麼？");
                p1.put("authorId", "author_system");
                p1.put("authorName", "Android架構師");
                p1.put("category", "hot");
                p1.put("security", "00");
                p1.put("isNeedPassword", false);
                p1.put("isCanPreviewResult", true);
                p1.put("isUserCanAddOption", false);
                p1.put("minOption", 1);
                p1.put("maxOption", 1);
                p1.put("optionCount", 4);
                p1.put("totalVotes", 128);
                p1.put("createdAt", now - 1000 * 60 * 30);
                p1.put("startTime", now - 1000 * 60 * 30);
                p1.put("endTime", now + 86400000L * 30L);

                List<Map<String, Object>> p1Top = new ArrayList<>();
                Map<String, Object> p1Top1 = new HashMap<>();
                p1Top1.put("optionId", "opt_101");
                p1Top1.put("title", "MVI + Jetpack Compose");
                p1Top1.put("voteCount", 89);
                p1Top.add(p1Top1);
                Map<String, Object> p1Top2 = new HashMap<>();
                p1Top2.put("optionId", "opt_102");
                p1Top2.put("title", "傳統 MVVM + ViewBinding");
                p1Top2.put("voteCount", 27);
                p1Top.add(p1Top2);
                p1.put("topOptions", p1Top);
                batch.set(p1Ref, p1);

                batch.set(p1Ref.collection("options").document("opt_101"), createOptMap("opt_101", "MVI + Jetpack Compose", 89, 1));
                batch.set(p1Ref.collection("options").document("opt_102"), createOptMap("opt_102", "傳統 MVVM + ViewBinding", 27, 2));
                batch.set(p1Ref.collection("options").document("opt_103"), createOptMap("opt_103", "MVP (Model-View-Presenter)", 8, 3));
                batch.set(p1Ref.collection("options").document("opt_104"), createOptMap("opt_104", "Flutter / KMP 跨平台", 4, 4));

                // Poll 2
                String p2Id = "vote_002";
                DocumentReference p2Ref = firestore.collection("polls").document(p2Id);
                Map<String, Object> p2 = new HashMap<>();
                p2.put("pollId", p2Id);
                p2.put("title", "中午團隊聚餐想吃哪種類型？(複選最多 2 項)");
                p2.put("authorId", "author_system");
                p2.put("authorName", "總務小幫手");
                p2.put("category", "hot");
                p2.put("security", "00");
                p2.put("isNeedPassword", false);
                p2.put("isCanPreviewResult", true);
                p2.put("isUserCanAddOption", true);
                p2.put("minOption", 1);
                p2.put("maxOption", 2);
                p2.put("optionCount", 4);
                p2.put("totalVotes", 75);
                p2.put("createdAt", now - 1000 * 60 * 120);
                p2.put("startTime", now - 1000 * 60 * 120);
                p2.put("endTime", now + 86400000L * 30L);

                List<Map<String, Object>> p2Top = new ArrayList<>();
                Map<String, Object> p2Top1 = new HashMap<>();
                p2Top1.put("optionId", "opt_203");
                p2Top1.put("title", "韓式炸雞配年糕");
                p2Top1.put("voteCount", 30);
                p2Top.add(p2Top1);
                Map<String, Object> p2Top2 = new HashMap<>();
                p2Top2.put("optionId", "opt_201");
                p2Top2.put("title", "日式拉麵");
                p2Top2.put("voteCount", 22);
                p2Top.add(p2Top2);
                p2.put("topOptions", p2Top);
                batch.set(p2Ref, p2);

                batch.set(p2Ref.collection("options").document("opt_201"), createOptMap("opt_201", "日式拉麵", 22, 1));
                batch.set(p2Ref.collection("options").document("opt_202"), createOptMap("opt_202", "美式漢堡薯條", 18, 2));
                batch.set(p2Ref.collection("options").document("opt_203"), createOptMap("opt_203", "韓式炸雞配年糕", 30, 3));
                batch.set(p2Ref.collection("options").document("opt_204"), createOptMap("opt_204", "健康低卡溫沙拉", 5, 4));

                // Poll 3
                String p3Id = "vote_003";
                DocumentReference p3Ref = firestore.collection("polls").document(p3Id);
                Map<String, Object> p3 = new HashMap<>();
                p3.put("pollId", p3Id);
                p3.put("title", "週末程式黑客松最佳主題投票 (加密投票)");
                p3.put("authorId", "author_system");
                p3.put("authorName", "DevCommunity");
                p3.put("category", "new");
                p3.put("security", "00");
                p3.put("isNeedPassword", true);
                p3.put("password", "123");
                p3.put("isCanPreviewResult", true);
                p3.put("isUserCanAddOption", false);
                p3.put("minOption", 1);
                p3.put("maxOption", 1);
                p3.put("optionCount", 3);
                p3.put("totalVotes", 16);
                p3.put("createdAt", now - 1000 * 60 * 10);
                p3.put("startTime", now - 1000 * 60 * 10);
                p3.put("endTime", now + 86400000L * 30L);
                batch.set(p3Ref, p3);

                batch.set(p3Ref.collection("options").document("opt_301"), createOptMap("opt_301", "AI Agentic Coding 應用開發", 10, 1));
                batch.set(p3Ref.collection("options").document("opt_302"), createOptMap("opt_302", "Web3 與去中心化身分驗證", 4, 2));
                batch.set(p3Ref.collection("options").document("opt_303"), createOptMap("opt_303", "邊緣運算 (Edge AI) 智慧相機", 2, 3));

                // Poll 4
                String p4Id = "vote_004";
                DocumentReference p4Ref = firestore.collection("polls").document(p4Id);
                Map<String, Object> p4 = new HashMap<>();
                p4.put("pollId", p4Id);
                p4.put("title", "你平常最常使用的 Kotlin 異步機制？");
                p4.put("authorId", "author_system");
                p4.put("authorName", "KotlinFan");
                p4.put("category", "new");
                p4.put("security", "00");
                p4.put("isNeedPassword", false);
                p4.put("isCanPreviewResult", true);
                p4.put("isUserCanAddOption", false);
                p4.put("minOption", 1);
                p4.put("maxOption", 1);
                p4.put("optionCount", 4);
                p4.put("totalVotes", 67);
                p4.put("createdAt", now - 1000 * 60 * 300);
                p4.put("startTime", now - 1000 * 60 * 300);
                p4.put("endTime", now + 86400000L * 30L);
                batch.set(p4Ref, p4);

                batch.set(p4Ref.collection("options").document("opt_401"), createOptMap("opt_401", "StateFlow / SharedFlow", 48, 1));
                batch.set(p4Ref.collection("options").document("opt_402"), createOptMap("opt_402", "Channel", 12, 2));
                batch.set(p4Ref.collection("options").document("opt_403"), createOptMap("opt_403", "RxJava 2 / 3", 5, 3));
                batch.set(p4Ref.collection("options").document("opt_404"), createOptMap("opt_404", "傳統 Callback 介面", 2, 4));

                batch.commit().addOnCompleteListener(bTask -> {
                    if (onDone != null) onDone.run();
                });
            } else {
                if (onDone != null) onDone.run();
            }
        });
    }

    private Map<String, Object> createOptMap(String id, String title, int count, int order) {
        Map<String, Object> map = new HashMap<>();
        map.put("optionId", id);
        map.put("title", title);
        map.put("voteCount", count);
        map.put("displayOrder", order);
        map.put("creatorId", "system");
        map.put("createdAt", System.currentTimeMillis());
        return map;
    }

    @Override
    public void getVoteData(final String voteCode, final User user, @Nullable final GetVoteDataCallback callback) {
        if (TextUtils.isEmpty(voteCode)) {
            if (callback != null) callback.onVoteDataNotAvailable();
            return;
        }

        final DocumentReference pollRef = firestore.collection("polls").document(voteCode);
        pollRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                if (callback != null) callback.onVoteDataNotAvailable();
                return;
            }

            final DocumentSnapshot pollDoc = task.getResult();
            final VoteData voteData = mapDocToVoteData(pollDoc);
            if (voteData == null) {
                if (callback != null) callback.onVoteDataNotAvailable();
                return;
            }

            final String uid = getUid(user);

            // Fetch options, voters status, and favorite status in parallel
            Task<QuerySnapshot> optionsTask = pollRef.collection("options").orderBy("displayOrder").get();
            Task<DocumentSnapshot> voterTask = pollRef.collection("voters").document(uid).get();
            Task<DocumentSnapshot> favTask = firestore.collection("users").document(uid)
                    .collection("favorites").document(voteCode).get();

            Tasks.whenAllComplete(optionsTask, voterTask, favTask).addOnCompleteListener(allTasks -> {
                boolean isVoted = false;
                List<String> chosenCodes = new ArrayList<>();
                if (voterTask.isSuccessful() && voterTask.getResult() != null && voterTask.getResult().exists()) {
                    isVoted = true;
                    List<String> list = (List<String>) voterTask.getResult().get("selectedOptionCodes");
                    if (list != null) {
                        chosenCodes.addAll(list);
                    }
                }
                voteData.setIsPolled(isVoted);

                boolean isFav = false;
                if (favTask.isSuccessful() && favTask.getResult() != null && favTask.getResult().exists()) {
                    isFav = true;
                }
                voteData.setIsFavorite(isFav);

                List<Option> optionList = new ArrayList<>();
                if (optionsTask.isSuccessful() && optionsTask.getResult() != null) {
                    for (DocumentSnapshot optDoc : optionsTask.getResult().getDocuments()) {
                        Option opt = new Option();
                        opt.setVoteCode(voteCode);
                        opt.setCode(optDoc.getId());
                        opt.setTitle(optDoc.getString("title"));
                        Long cnt = optDoc.getLong("voteCount");
                        opt.setCount(cnt != null ? cnt.intValue() : 0);
                        if (chosenCodes.contains(opt.getCode())) {
                            opt.setIsUserChoiced(true);
                            voteData.setOptionUserChoiceCode(opt.getCode());
                            voteData.setOptionUserChoiceTitle(opt.getTitle());
                            voteData.setOptionUserChoiceCount(opt.getCount());
                        }
                        optionList.add(opt);
                    }
                }

                voteData.setOptions(optionList);
                voteData.setNetOptions(optionList);
                if (callback != null) callback.onVoteDataLoaded(voteData);
            });
        });
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        // Only for local save
    }

    @Override
    public void getOptions(VoteData voteData, GetVoteOptionsCallback callback) {
        // Only for local save
    }

    @Override
    public void saveOptions(List<Option> optionList) {
        // Only for local save
    }

    @Override
    public void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab) {
        // Only for local save
    }

    @Override
    public void addNewOption(final String voteCode, final String password, final List<String> newOptions
            , final User user, final AddNewOptionCallback callback) {
        if (newOptions == null || newOptions.isEmpty()) {
            if (callback != null) callback.onFailure();
            return;
        }

        final DocumentReference pollRef = firestore.collection("polls").document(voteCode);
        pollRef.get().addOnSuccessListener(pollDoc -> {
            if (!pollDoc.exists()) {
                if (callback != null) callback.onFailure();
                return;
            }

            Boolean isNeedPw = pollDoc.getBoolean("isNeedPassword");
            if (Boolean.TRUE.equals(isNeedPw)) {
                String storedPw = pollDoc.getString("password");
                if (storedPw != null && !storedPw.equals(password)) {
                    if (callback != null) callback.onPasswordInvalid();
                    return;
                }
            }

            final String uid = getUid(user);
            WriteBatch batch = firestore.batch();
            long now = System.currentTimeMillis();

            for (int i = 0; i < newOptions.size(); i++) {
                String optId = "opt_" + now + "_" + (i + 1);
                DocumentReference optRef = pollRef.collection("options").document(optId);
                Map<String, Object> optData = new HashMap<>();
                optData.put("optionId", optId);
                optData.put("title", newOptions.get(i));
                optData.put("voteCount", 0);
                optData.put("displayOrder", 99 + i);
                optData.put("creatorId", uid);
                optData.put("createdAt", now);
                batch.set(optRef, optData);
            }

            batch.update(pollRef, "optionCount", FieldValue.increment(newOptions.size()));
            batch.commit().addOnSuccessListener(aVoid -> {
                getVoteData(voteCode, user, new GetVoteDataCallback() {
                    @Override
                    public void onVoteDataLoaded(VoteData voteData) {
                        if (callback != null) callback.onSuccess(voteData);
                    }

                    @Override
                    public void onVoteDataNotAvailable() {
                        if (callback != null) callback.onFailure();
                    }
                });
            }).addOnFailureListener(e -> {
                if (callback != null) callback.onFailure();
            });
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onFailure();
        });
    }

    @Override
    public void pollVote(@NonNull final String voteCode, final String password, @NonNull final List<String> pollOptions
            , @NonNull final User user, @Nullable final PollVoteCallback callback) {
        final String uid = getUid(user);
        final DocumentReference pollRef = firestore.collection("polls").document(voteCode);
        final DocumentReference voterRef = pollRef.collection("voters").document(uid);
        final DocumentReference userVotedRef = firestore.collection("users").document(uid)
                .collection("voted_polls").document(voteCode);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot pollDoc = transaction.get(pollRef);
            if (!pollDoc.exists()) {
                throw new IllegalStateException("Poll not found");
            }

            Boolean isNeedPw = pollDoc.getBoolean("isNeedPassword");
            if (Boolean.TRUE.equals(isNeedPw)) {
                String storedPw = pollDoc.getString("password");
                if (storedPw != null && !storedPw.equals(password)) {
                    throw new IllegalArgumentException("error_invalid_password");
                }
            }

            DocumentSnapshot voterDoc = transaction.get(voterRef);
            if (voterDoc.exists()) {
                throw new IllegalStateException("Already voted");
            }

            long now = System.currentTimeMillis();

            // 1. Write voter record
            Map<String, Object> voterData = new HashMap<>();
            voterData.put("userId", uid);
            voterData.put("selectedOptionCodes", pollOptions);
            voterData.put("votedAt", now);
            transaction.set(voterRef, voterData);

            // 2. Increment option counts
            for (String optCode : pollOptions) {
                DocumentReference optRef = pollRef.collection("options").document(optCode);
                transaction.update(optRef, "voteCount", FieldValue.increment(1));
            }

            // 3. Increment total votes
            transaction.update(pollRef, "totalVotes", FieldValue.increment(pollOptions.size()));

            // 4. Record to user voted history
            Map<String, Object> userVotedData = new HashMap<>();
            userVotedData.put("pollId", voteCode);
            userVotedData.put("selectedOptionCodes", pollOptions);
            userVotedData.put("votedAt", now);
            transaction.set(userVotedRef, userVotedData);

            return null;
        }).addOnSuccessListener(aVoid -> {
            getVoteData(voteCode, user, new GetVoteDataCallback() {
                @Override
                public void onVoteDataLoaded(VoteData voteData) {
                    if (callback != null) callback.onSuccess(voteData);
                }

                @Override
                public void onVoteDataNotAvailable() {
                    if (callback != null) callback.onFailure();
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "pollVote failed: " + e.getMessage());
            if (callback != null) {
                if (e instanceof IllegalArgumentException && "error_invalid_password".equals(e.getMessage())) {
                    callback.onPasswordInvalid();
                } else {
                    callback.onFailure();
                }
            }
        });
    }

    @Override
    public void favoriteVote(final String voteCode, final boolean isFavorite, User user, final FavoriteVoteCallback callback) {
        final String uid = getUid(user);
        final DocumentReference favRef = firestore.collection("users").document(uid)
                .collection("favorites").document(voteCode);

        if (isFavorite) {
            Map<String, Object> favData = new HashMap<>();
            favData.put("pollId", voteCode);
            favData.put("createdAt", System.currentTimeMillis());
            favRef.set(favData).addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess(true);
            }).addOnFailureListener(e -> {
                if (callback != null) callback.onFailure();
            });
        } else {
            favRef.delete().addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess(false);
            }).addOnFailureListener(e -> {
                if (callback != null) callback.onFailure();
            });
        }
    }

    @Override
    public void createVote(@NonNull final VoteData voteSetting, @NonNull final List<String> options
            , final File image, final GetVoteDataCallback callback) {
        final String pollId = (voteSetting.getVoteCode() != null && !voteSetting.getVoteCode().isEmpty())
                ? voteSetting.getVoteCode() : "vote_" + UUID.randomUUID().toString().substring(0, 8);
        final String uid = getUid(voteSetting.author);
        final String authorName = (voteSetting.getAuthorName() != null && !voteSetting.getAuthorName().isEmpty())
                ? voteSetting.getAuthorName() : (voteSetting.author != null ? voteSetting.author.getUserName() : "匿名");

        if (image != null && image.exists()) {
            // Upload image to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("poll_images/" + pollId + ".jpg");
            storageRef.putFile(Uri.fromFile(image)).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        commitCreateVote(pollId, voteSetting, options, uri.toString(), uid, authorName, callback);
                    }).addOnFailureListener(e -> {
                        commitCreateVote(pollId, voteSetting, options, null, uid, authorName, callback);
                    });
                } else {
                    commitCreateVote(pollId, voteSetting, options, null, uid, authorName, callback);
                }
            });
        } else {
            commitCreateVote(pollId, voteSetting, options, null, uid, authorName, callback);
        }
    }

    private void commitCreateVote(final String pollId, final VoteData voteSetting, final List<String> options
            , final String imageUrl, final String uid, final String authorName, final GetVoteDataCallback callback) {
        final DocumentReference pollRef = firestore.collection("polls").document(pollId);
        WriteBatch batch = firestore.batch();
        long now = System.currentTimeMillis();

        Map<String, Object> pollDoc = new HashMap<>();
        pollDoc.put("pollId", pollId);
        pollDoc.put("title", voteSetting.getTitle());
        pollDoc.put("authorId", uid);
        pollDoc.put("authorName", authorName);
        pollDoc.put("authorIcon", voteSetting.getAuthorIcon());
        pollDoc.put("imageUrl", imageUrl);
        pollDoc.put("category", "hot");
        pollDoc.put("security", voteSetting.getSecurity() != null ? voteSetting.getSecurity() : VoteData.SECURITY_PUBLIC);
        pollDoc.put("isNeedPassword", voteSetting.getIsNeedPassword());
        if (voteSetting.getIsNeedPassword() && !TextUtils.isEmpty(voteSetting.password)) {
            pollDoc.put("password", voteSetting.password);
        }
        pollDoc.put("isCanPreviewResult", voteSetting.getIsCanPreviewResult());
        pollDoc.put("isUserCanAddOption", voteSetting.getIsUserCanAddOption());
        pollDoc.put("minOption", voteSetting.getMinOption() > 0 ? voteSetting.getMinOption() : 1);
        pollDoc.put("maxOption", voteSetting.getMaxOption() > 0 ? voteSetting.getMaxOption() : 1);
        pollDoc.put("optionCount", options.size());
        pollDoc.put("totalVotes", 0);
        long startT = voteSetting.getStartTime() > 0 ? voteSetting.getStartTime() : now;
        long endT = voteSetting.getEndTime() > 0 ? voteSetting.getEndTime() : (now + 86400000L * 30L);
        pollDoc.put("startTime", startT);
        pollDoc.put("endTime", endT);
        pollDoc.put("createdAt", now);

        List<Map<String, Object>> topOptions = new ArrayList<>();
        for (int i = 0; i < Math.min(2, options.size()); i++) {
            Map<String, Object> optMap = new HashMap<>();
            optMap.put("optionId", "opt_" + (i + 1));
            optMap.put("title", options.get(i));
            optMap.put("voteCount", 0);
            topOptions.add(optMap);
        }
        pollDoc.put("topOptions", topOptions);
        batch.set(pollRef, pollDoc);

        for (int i = 0; i < options.size(); i++) {
            String optId = "opt_" + (i + 1);
            DocumentReference optRef = pollRef.collection("options").document(optId);
            Map<String, Object> optData = new HashMap<>();
            optData.put("optionId", optId);
            optData.put("title", options.get(i));
            optData.put("voteCount", 0);
            optData.put("displayOrder", i + 1);
            optData.put("creatorId", uid);
            optData.put("createdAt", now);
            batch.set(optRef, optData);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            User creator = new User();
            creator.setUserCode(uid);
            creator.setUserName(authorName);
            getVoteData(pollId, creator, callback);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onVoteDataNotAvailable();
        });
    }

    @Override
    public void getHotVoteList(final int offset, final User user, final GetVoteListCallback callback) {
        seedInitialDataIfEmpty(() -> {
            firestore.collection("polls")
                    .orderBy("totalVotes", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<VoteData> list = new ArrayList<>();
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                VoteData data = mapDocToVoteData(doc);
                                if (data != null && VoteData.SECURITY_PUBLIC.equals(data.getSecurity())) {
                                    list.add(data);
                                }
                            }
                        }
                        if (callback != null) callback.onVoteListLoaded(list);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getHotVoteList failed: " + e.getMessage());
                        if (callback != null) callback.onVoteListNotAvailable();
                    });
        });
    }

    @Override
    public void getNewVoteList(final int offset, final User user, final GetVoteListCallback callback) {
        seedInitialDataIfEmpty(() -> {
            firestore.collection("polls")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<VoteData> list = new ArrayList<>();
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                VoteData data = mapDocToVoteData(doc);
                                if (data != null && VoteData.SECURITY_PUBLIC.equals(data.getSecurity())) {
                                    list.add(data);
                                }
                            }
                        }
                        if (callback != null) callback.onVoteListLoaded(list);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getNewVoteList failed: " + e.getMessage());
                        if (callback != null) callback.onVoteListNotAvailable();
                    });
        });
    }

    @Override
    public void getCreateVoteList(final int offset, final User loginUser, final User targetUser, final GetVoteListCallback callback) {
        final String uid = targetUser != null && !TextUtils.isEmpty(targetUser.getUserCode())
                ? targetUser.getUserCode() : getUid(loginUser);

        firestore.collection("polls")
                .whereEqualTo("authorId", uid)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<VoteData> list = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            VoteData data = mapDocToVoteData(doc);
                            if (data != null) {
                                list.add(data);
                            }
                        }
                    }
                    Collections.sort(list, (o1, o2) -> Long.compare(o2.getStartTime(), o1.getStartTime()));
                    if (callback != null) callback.onVoteListLoaded(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getCreateVoteList failed: " + e.getMessage());
                    if (callback != null) callback.onVoteListNotAvailable();
                });
    }

    @Override
    public void getParticipateVoteList(final int offset, final User loginUser, final User targetUser, final GetVoteListCallback callback) {
        final String uid = targetUser != null && !TextUtils.isEmpty(targetUser.getUserCode())
                ? targetUser.getUserCode() : getUid(loginUser);

        firestore.collection("users").document(uid).collection("voted_polls").limit(50).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) {
                        if (callback != null) callback.onVoteListLoaded(new ArrayList<>());
                        return;
                    }
                    List<String> pollIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        pollIds.add(doc.getId());
                    }

                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (String id : pollIds) {
                        tasks.add(firestore.collection("polls").document(id).get());
                    }

                    Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                        List<VoteData> list = new ArrayList<>();
                        for (Task<DocumentSnapshot> t : tasks) {
                            if (t.isSuccessful() && t.getResult() != null && t.getResult().exists()) {
                                VoteData data = mapDocToVoteData(t.getResult());
                                if (data != null) {
                                    data.setIsPolled(true);
                                    list.add(data);
                                }
                            }
                        }
                        if (callback != null) callback.onVoteListLoaded(list);
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onVoteListNotAvailable();
                });
    }

    @Override
    public void getFavoriteVoteList(final int offset, final User loginUser, final User targetUser, final GetVoteListCallback callback) {
        final String uid = targetUser != null && !TextUtils.isEmpty(targetUser.getUserCode())
                ? targetUser.getUserCode() : getUid(loginUser);

        firestore.collection("users").document(uid).collection("favorites").limit(50).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) {
                        if (callback != null) callback.onVoteListLoaded(new ArrayList<>());
                        return;
                    }
                    List<String> pollIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        pollIds.add(doc.getId());
                    }

                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (String id : pollIds) {
                        tasks.add(firestore.collection("polls").document(id).get());
                    }

                    Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                        List<VoteData> list = new ArrayList<>();
                        for (Task<DocumentSnapshot> t : tasks) {
                            if (t.isSuccessful() && t.getResult() != null && t.getResult().exists()) {
                                VoteData data = mapDocToVoteData(t.getResult());
                                if (data != null) {
                                    data.setIsFavorite(true);
                                    list.add(data);
                                }
                            }
                        }
                        if (callback != null) callback.onVoteListLoaded(list);
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onVoteListNotAvailable();
                });
    }

    @Override
    public void getSearchVoteList(final String keyword, final int offset, @NonNull final User user, final GetVoteListCallback callback) {
        firestore.collection("polls")
                .limit(100)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<VoteData> list = new ArrayList<>();
                    if (snapshot != null) {
                        String query = keyword != null ? keyword.toLowerCase().trim() : "";
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            VoteData data = mapDocToVoteData(doc);
                            if (data != null && VoteData.SECURITY_PUBLIC.equals(data.getSecurity())) {
                                if (TextUtils.isEmpty(query) || (data.getTitle() != null && data.getTitle().toLowerCase().contains(query))) {
                                    list.add(data);
                                }
                            }
                        }
                    }
                    if (callback != null) callback.onVoteListLoaded(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onVoteListNotAvailable();
                });
    }
}
