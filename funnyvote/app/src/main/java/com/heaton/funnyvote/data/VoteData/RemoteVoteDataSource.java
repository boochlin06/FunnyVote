package com.heaton.funnyvote.data.VoteData;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Observable;

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

        List<Map<String, Object>> topOptions = (List<Map<String, Object>>) doc.get("topOptions");
        if (topOptions != null && !topOptions.isEmpty()) {
            if (topOptions.size() > 0) {
                Map<String, Object> o1 = topOptions.get(0);
                voteData.setOption1Code((String) o1.get("optionId"));
                voteData.setOption1Title((String) o1.get("title"));
                Long c1 = (Long) o1.get("voteCount");
                voteData.setOption1Count(c1 != null ? c1.intValue() : 0);
            }
            if (topOptions.size() > 1) {
                Map<String, Object> o2 = topOptions.get(1);
                voteData.setOption2Code((String) o2.get("optionId"));
                voteData.setOption2Title((String) o2.get("title"));
                Long c2 = (Long) o2.get("voteCount");
                voteData.setOption2Count(c2 != null ? c2.intValue() : 0);
            }
        }
        return voteData;
    }

    private synchronized void seedInitialDataIfEmpty(final Runnable onDone) {
        if (hasCheckedSeed) {
            if (onDone != null) onDone.run();
            return;
        }
        hasCheckedSeed = true;

        firestore.collection("polls").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && (task.getResult() == null || task.getResult().isEmpty())) {
                Log.d(TAG, "Database is empty, seeding initial polls...");
                WriteBatch batch = firestore.batch();
                long now = System.currentTimeMillis();

                DocumentReference p1Ref = firestore.collection("polls").document("poll_android_arch_2026");
                Map<String, Object> p1 = new HashMap<>();
                p1.put("pollId", "poll_android_arch_2026");
                p1.put("title", "2026 年你最推薦的 Android 架構模式？");
                p1.put("authorId", "author_heaton");
                p1.put("authorName", "Heaton");
                p1.put("authorIcon", "https://api.dicebear.com/7.x/bottts/png?seed=heaton");
                p1.put("imageUrl", "https://picsum.photos/seed/android_arch/600/400");
                p1.put("category", "hot");
                p1.put("security", VoteData.SECURITY_PUBLIC);
                p1.put("isNeedPassword", false);
                p1.put("isCanPreviewResult", true);
                p1.put("isUserCanAddOption", true);
                p1.put("minOption", 1);
                p1.put("maxOption", 1);
                p1.put("optionCount", 4);
                p1.put("totalVotes", 128);
                p1.put("createdAt", now);
                p1.put("startTime", now);
                p1.put("endTime", now + 86400000L * 30L);
                batch.set(p1Ref, p1);

                batch.set(p1Ref.collection("options").document("opt_101"), createOptMap("opt_101", "MVI + Jetpack Compose (Modern Android)", 85, 1));
                batch.set(p1Ref.collection("options").document("opt_102"), createOptMap("opt_102", "Clean Architecture + Flow/Coroutines", 32, 2));
                batch.set(p1Ref.collection("options").document("opt_103"), createOptMap("opt_103", "經典 MVP + RxJava (堅固耐用)", 8, 3));
                batch.set(p1Ref.collection("options").document("opt_104"), createOptMap("opt_104", "MVVM + LiveData / DataBinding", 3, 4));

                DocumentReference p2Ref = firestore.collection("polls").document("poll_tech_lunch_today");
                Map<String, Object> p2 = new HashMap<>();
                p2.put("pollId", "poll_tech_lunch_today");
                p2.put("title", "工程師中午吃什麼？（終極決選）");
                p2.put("authorId", "author_alice");
                p2.put("authorName", "Alice Chen");
                p2.put("authorIcon", "https://api.dicebear.com/7.x/bottts/png?seed=alice");
                p2.put("imageUrl", "https://picsum.photos/seed/food_lunch/600/400");
                p2.put("category", "hot");
                p2.put("security", VoteData.SECURITY_PUBLIC);
                p2.put("isNeedPassword", false);
                p2.put("isCanPreviewResult", true);
                p2.put("isUserCanAddOption", true);
                p2.put("minOption", 1);
                p2.put("maxOption", 2);
                p2.put("optionCount", 4);
                p2.put("totalVotes", 95);
                p2.put("createdAt", now - 1000 * 60 * 60);
                p2.put("startTime", now - 1000 * 60 * 60);
                p2.put("endTime", now + 86400000L * 30L);
                batch.set(p2Ref, p2);

                batch.set(p2Ref.collection("options").document("opt_201"), createOptMap("opt_201", "拉麵 (豚骨濃湯/沾麵)", 45, 1));
                batch.set(p2Ref.collection("options").document("opt_202"), createOptMap("opt_202", "麥當勞 1+1 / 大麥克餐", 30, 2));
                batch.set(p2Ref.collection("options").document("opt_203"), createOptMap("opt_203", "健康健康餐盒 (舒肥雞胸肉)", 15, 3));
                batch.set(p2Ref.collection("options").document("opt_204"), createOptMap("opt_204", "超商御飯糰 + 燕麥奶", 5, 4));

                DocumentReference p3Ref = firestore.collection("polls").document("poll_funnyvote_upgrade");
                Map<String, Object> p3 = new HashMap<>();
                p3.put("pollId", "poll_funnyvote_upgrade");
                p3.put("title", "FunnyVote 四大經典分支重構，你最愛哪一個？");
                p3.put("authorId", "author_heaton");
                p3.put("authorName", "Heaton");
                p3.put("authorIcon", "https://api.dicebear.com/7.x/bottts/png?seed=heaton");
                p3.put("imageUrl", "https://picsum.photos/seed/funnyvote/600/400");
                p3.put("category", "hot");
                p3.put("security", VoteData.SECURITY_PUBLIC);
                p3.put("isNeedPassword", false);
                p3.put("isCanPreviewResult", true);
                p3.put("isUserCanAddOption", false);
                p3.put("minOption", 1);
                p3.put("maxOption", 1);
                p3.put("optionCount", 4);
                p3.put("totalVotes", 256);
                p3.put("createdAt", now - 1000 * 60 * 120);
                p3.put("startTime", now - 1000 * 60 * 120);
                p3.put("endTime", now + 86400000L * 30L);
                batch.set(p3Ref, p3);

                batch.set(p3Ref.collection("options").document("opt_301"), createOptMap("opt_301", "mvp_firebase (原生極簡 MVP)", 64, 1));
                batch.set(p3Ref.collection("options").document("opt_302"), createOptMap("opt_302", "mvp_dagger_firebase (現代 Dagger 2 依賴注入)", 72, 2));
                batch.set(p3Ref.collection("options").document("opt_303"), createOptMap("opt_303", "mvp_rxjava_firebase (RxJava 響應式流)", 88, 3));
                batch.set(p3Ref.collection("options").document("opt_304"), createOptMap("opt_304", "mvp_kotlin_firebase (Kotlin 現代化)", 32, 4));

                DocumentReference p4Ref = firestore.collection("polls").document("poll_state_management");
                Map<String, Object> p4 = new HashMap<>();
                p4.put("pollId", "poll_state_management");
                p4.put("title", "Android 非同步與狀態流轉首選工具？");
                p4.put("authorId", "author_bob");
                p4.put("authorName", "Bob");
                p4.put("authorIcon", "https://api.dicebear.com/7.x/bottts/png?seed=bob");
                p4.put("imageUrl", "https://picsum.photos/seed/state/600/400");
                p4.put("category", "hot");
                p4.put("security", VoteData.SECURITY_PUBLIC);
                p4.put("isNeedPassword", false);
                p4.put("isCanPreviewResult", true);
                p4.put("isUserCanAddOption", true);
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
    public Observable<VoteData> getVoteData(final String voteCode, final User user) {
        return Observable.create(emitter -> {
            if (TextUtils.isEmpty(voteCode)) {
                if (!emitter.isDisposed()) emitter.onError(new IllegalArgumentException("voteCode is empty"));
                return;
            }

            final DocumentReference pollRef = firestore.collection("polls").document(voteCode);
            pollRef.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    if (!emitter.isDisposed()) emitter.onError(new Exception("Vote not found: " + voteCode));
                    return;
                }

                final DocumentSnapshot pollDoc = task.getResult();
                final VoteData voteData = mapDocToVoteData(pollDoc);
                if (voteData == null) {
                    if (!emitter.isDisposed()) emitter.onError(new Exception("Failed to parse VoteData"));
                    return;
                }

                final String uid = getUid(user);

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
                    if (!emitter.isDisposed()) {
                        emitter.onNext(voteData);
                        emitter.onComplete();
                    }
                });
            });
        });
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        // Only for local save
    }

    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        if (voteData == null || TextUtils.isEmpty(voteData.getVoteCode())) {
            return Observable.just(new ArrayList<>());
        }
        return Observable.create(emitter -> {
            firestore.collection("polls").document(voteData.getVoteCode())
                    .collection("options").orderBy("displayOrder").get()
                    .addOnSuccessListener(snapshot -> {
                        List<Option> optionList = new ArrayList<>();
                        if (snapshot != null) {
                            for (DocumentSnapshot optDoc : snapshot.getDocuments()) {
                                Option opt = new Option();
                                opt.setVoteCode(voteData.getVoteCode());
                                opt.setCode(optDoc.getId());
                                opt.setTitle(optDoc.getString("title"));
                                Long cnt = optDoc.getLong("voteCount");
                                opt.setCount(cnt != null ? cnt.intValue() : 0);
                                optionList.add(opt);
                            }
                        }
                        if (!emitter.isDisposed()) {
                            emitter.onNext(optionList);
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) emitter.onError(e);
                    });
        });
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
    public Observable<VoteData> addNewOption(final String voteCode, final String password, final List<String> newOptions, final User user) {
        if (newOptions == null || newOptions.isEmpty()) {
            return Observable.error(new IllegalArgumentException("New options cannot be empty"));
        }

        return Observable.create(emitter -> {
            final DocumentReference pollRef = firestore.collection("polls").document(voteCode);
            pollRef.get().addOnSuccessListener(pollDoc -> {
                if (!pollDoc.exists()) {
                    if (!emitter.isDisposed()) emitter.onError(new Exception("Vote not found"));
                    return;
                }

                Boolean isNeedPw = pollDoc.getBoolean("isNeedPassword");
                if (Boolean.TRUE.equals(isNeedPw)) {
                    String storedPw = pollDoc.getString("password");
                    if (storedPw != null && !storedPw.equals(password)) {
                        if (!emitter.isDisposed()) emitter.onError(new Exception("error_invalid_password"));
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
                    getVoteData(voteCode, user).subscribe(
                            data -> {
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(data);
                                    emitter.onComplete();
                                }
                            },
                            error -> {
                                if (!emitter.isDisposed()) emitter.onError(error);
                            }
                    );
                }).addOnFailureListener(e -> {
                    if (!emitter.isDisposed()) emitter.onError(e);
                });
            }).addOnFailureListener(e -> {
                if (!emitter.isDisposed()) emitter.onError(e);
            });
        });
    }

    @Override
    public Observable<VoteData> pollVote(@NonNull final String voteCode, final String password,
                                         @NonNull final List<String> pollOptions, @NonNull final User user) {
        return Observable.create(emitter -> {
            final DocumentReference pollRef = firestore.collection("polls").document(voteCode);
            final String uid = getUid(user);

            firestore.runTransaction(transaction -> {
                DocumentSnapshot pollDoc = transaction.get(pollRef);
                if (!pollDoc.exists()) {
                    throw new RuntimeException("Poll does not exist");
                }

                Boolean isNeedPw = pollDoc.getBoolean("isNeedPassword");
                if (Boolean.TRUE.equals(isNeedPw)) {
                    String storedPw = pollDoc.getString("password");
                    if (storedPw != null && !storedPw.equals(password)) {
                        throw new RuntimeException("error_invalid_password");
                    }
                }

                for (String optCode : pollOptions) {
                    DocumentReference optRef = pollRef.collection("options").document(optCode);
                    transaction.update(optRef, "voteCount", FieldValue.increment(1));
                }

                transaction.update(pollRef, "totalVotes", FieldValue.increment(1));

                DocumentReference voterRef = pollRef.collection("voters").document(uid);
                Map<String, Object> voterData = new HashMap<>();
                voterData.put("userId", uid);
                voterData.put("votedAt", System.currentTimeMillis());
                voterData.put("selectedOptionCodes", pollOptions);
                transaction.set(voterRef, voterData, SetOptions.merge());

                DocumentReference userVotedRef = firestore.collection("users").document(uid)
                        .collection("voted_polls").document(voteCode);
                Map<String, Object> uVotedData = new HashMap<>();
                uVotedData.put("pollId", voteCode);
                uVotedData.put("votedAt", System.currentTimeMillis());
                transaction.set(userVotedRef, uVotedData, SetOptions.merge());

                return null;
            }).addOnSuccessListener(result -> {
                getVoteData(voteCode, user).subscribe(
                        data -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(data);
                                emitter.onComplete();
                            }
                        },
                        error -> {
                            if (!emitter.isDisposed()) emitter.onError(error);
                        }
                );
            }).addOnFailureListener(e -> {
                if (!emitter.isDisposed()) emitter.onError(e);
            });
        });
    }

    @Override
    public Observable<Boolean> favoriteVote(final String voteCode, final boolean isFavorite, final User user) {
        return Observable.create(emitter -> {
            final String uid = getUid(user);
            DocumentReference favRef = firestore.collection("users").document(uid)
                    .collection("favorites").document(voteCode);

            if (isFavorite) {
                Map<String, Object> favData = new HashMap<>();
                favData.put("pollId", voteCode);
                favData.put("createdAt", System.currentTimeMillis());
                favRef.set(favData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(true);
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (!emitter.isDisposed()) emitter.onError(e);
                        });
            } else {
                favRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(false);
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (!emitter.isDisposed()) emitter.onError(e);
                        });
            }
        });
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {
        // Nothing to do
    }

    @Override
    public Observable<VoteData> createVote(@NonNull final VoteData voteSetting, @NonNull final List<String> options, final File image) {
        return Observable.create(emitter -> {
            final String pollId = "poll_" + System.currentTimeMillis();
            final String uid = !TextUtils.isEmpty(voteSetting.getAuthorCode()) ? voteSetting.getAuthorCode() : getUid(null);

            if (image != null && image.exists()) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("polls/" + pollId + ".jpg");
                storageRef.putFile(Uri.fromFile(image))
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            voteSetting.setVoteImage(uri.toString());
                            saveNewPollToFirestore(pollId, uid, voteSetting, options, emitter);
                        }).addOnFailureListener(e -> {
                            saveNewPollToFirestore(pollId, uid, voteSetting, options, emitter);
                        }))
                        .addOnFailureListener(e -> {
                            saveNewPollToFirestore(pollId, uid, voteSetting, options, emitter);
                        });
            } else {
                saveNewPollToFirestore(pollId, uid, voteSetting, options, emitter);
            }
        });
    }

    private void saveNewPollToFirestore(String pollId, String uid, VoteData voteSetting, List<String> options,
                                        io.reactivex.ObservableEmitter<VoteData> emitter) {
        long now = System.currentTimeMillis();
        DocumentReference pollRef = firestore.collection("polls").document(pollId);

        Map<String, Object> pollMap = new HashMap<>();
        pollMap.put("pollId", pollId);
        pollMap.put("title", voteSetting.getTitle());
        pollMap.put("authorId", uid);
        pollMap.put("authorName", !TextUtils.isEmpty(voteSetting.getAuthorName()) ? voteSetting.getAuthorName() : "訪客");
        pollMap.put("authorIcon", !TextUtils.isEmpty(voteSetting.getAuthorIcon()) ? voteSetting.getAuthorIcon() : "");
        pollMap.put("imageUrl", !TextUtils.isEmpty(voteSetting.getVoteImage()) ? voteSetting.getVoteImage() : "");
        pollMap.put("category", "hot");
        pollMap.put("security", !TextUtils.isEmpty(voteSetting.getSecurity()) ? voteSetting.getSecurity() : VoteData.SECURITY_PUBLIC);
        pollMap.put("isNeedPassword", voteSetting.getIsNeedPassword());
        pollMap.put("password", voteSetting.getIsNeedPassword() ? "1234" : "");
        pollMap.put("isCanPreviewResult", voteSetting.getIsCanPreviewResult());
        pollMap.put("isUserCanAddOption", voteSetting.getIsUserCanAddOption());
        pollMap.put("minOption", voteSetting.getMinOption() > 0 ? voteSetting.getMinOption() : 1);
        pollMap.put("maxOption", voteSetting.getMaxOption() > 0 ? voteSetting.getMaxOption() : 1);
        pollMap.put("optionCount", options.size());
        pollMap.put("totalVotes", 0);
        pollMap.put("createdAt", now);
        pollMap.put("startTime", voteSetting.getStartTime() > 0 ? voteSetting.getStartTime() : now);
        pollMap.put("endTime", voteSetting.getEndTime() > 0 ? voteSetting.getEndTime() : now + 86400000L * 7L);

        List<Map<String, Object>> topOptions = new ArrayList<>();
        for (int i = 0; i < Math.min(options.size(), 2); i++) {
            Map<String, Object> opt = new HashMap<>();
            opt.put("optionId", "opt_" + (i + 1));
            opt.put("title", options.get(i));
            opt.put("voteCount", 0);
            topOptions.add(opt);
        }
        pollMap.put("topOptions", topOptions);

        WriteBatch batch = firestore.batch();
        batch.set(pollRef, pollMap);

        List<Option> entityOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            String optId = "opt_" + (i + 1);
            DocumentReference optRef = pollRef.collection("options").document(optId);
            Map<String, Object> optMap = new HashMap<>();
            optMap.put("optionId", optId);
            optMap.put("title", options.get(i));
            optMap.put("voteCount", 0);
            optMap.put("displayOrder", i + 1);
            optMap.put("creatorId", uid);
            optMap.put("createdAt", now);
            batch.set(optRef, optMap);

            Option o = new Option();
            o.setCode(optId);
            o.setTitle(options.get(i));
            o.setCount(0);
            o.setVoteCode(pollId);
            entityOptions.add(o);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            voteSetting.setVoteCode(pollId);
            voteSetting.setOptions(entityOptions);
            voteSetting.setNetOptions(entityOptions);
            voteSetting.setOptionCount(options.size());
            voteSetting.setPollCount(0);
            if (!emitter.isDisposed()) {
                emitter.onNext(voteSetting);
                emitter.onComplete();
            }
        }).addOnFailureListener(e -> {
            if (!emitter.isDisposed()) emitter.onError(e);
        });
    }

    @Override
    public Observable<List<VoteData>> getHotVoteList(final int offset, final User user) {
        return Observable.create(emitter -> {
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
                            if (!emitter.isDisposed()) {
                                emitter.onNext(list);
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "getHotVoteList failed: " + e.getMessage());
                            if (!emitter.isDisposed()) emitter.onError(e);
                        });
            });
        });
    }

    @Override
    public Observable<List<VoteData>> getNewVoteList(final int offset, final User user) {
        return Observable.create(emitter -> {
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
                            if (!emitter.isDisposed()) {
                                emitter.onNext(list);
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "getNewVoteList failed: " + e.getMessage());
                            if (!emitter.isDisposed()) emitter.onError(e);
                        });
            });
        });
    }

    @Override
    public Observable<List<VoteData>> getCreateVoteList(final int offset, final User loginUser, final User targetUser) {
        final String uid = targetUser != null && !TextUtils.isEmpty(targetUser.getUserCode())
                ? targetUser.getUserCode() : getUid(loginUser);

        return Observable.create(emitter -> {
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
                        if (!emitter.isDisposed()) {
                            emitter.onNext(list);
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getCreateVoteList failed: " + e.getMessage());
                        if (!emitter.isDisposed()) emitter.onError(e);
                    });
        });
    }

    @Override
    public Observable<List<VoteData>> getParticipateVoteList(final int offset, final User loginUser, final User targetUser) {
        final String uid = targetUser != null && !TextUtils.isEmpty(targetUser.getUserCode())
                ? targetUser.getUserCode() : getUid(loginUser);

        return Observable.create(emitter -> {
            firestore.collection("users").document(uid).collection("voted_polls").limit(50).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot == null || snapshot.isEmpty()) {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(new ArrayList<>());
                                emitter.onComplete();
                            }
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
                            if (!emitter.isDisposed()) {
                                emitter.onNext(list);
                                emitter.onComplete();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) emitter.onError(e);
                    });
        });
    }

    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(final int offset, final User loginUser, final User targetUser) {
        final String uid = targetUser != null && !TextUtils.isEmpty(targetUser.getUserCode())
                ? targetUser.getUserCode() : getUid(loginUser);

        return Observable.create(emitter -> {
            firestore.collection("users").document(uid).collection("favorites").limit(50).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot == null || snapshot.isEmpty()) {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(new ArrayList<>());
                                emitter.onComplete();
                            }
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
                            if (!emitter.isDisposed()) {
                                emitter.onNext(list);
                                emitter.onComplete();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) emitter.onError(e);
                    });
        });
    }

    @Override
    public Observable<List<VoteData>> getSearchVoteList(final String keyword, final int offset, @NonNull final User user) {
        return Observable.create(emitter -> {
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
                        if (!emitter.isDisposed()) {
                            emitter.onNext(list);
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) emitter.onError(e);
                    });
        });
    }
}

