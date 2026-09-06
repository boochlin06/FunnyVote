package com.heaton.funnyvote.data.promotion;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class RemotePromotionSource implements PromotionDataSource {

    private static final String TAG = RemotePromotionSource.class.getSimpleName();
    private static RemotePromotionSource INSTANCE = null;
    public static final int PAGE_COUNT = 10;

    public static RemotePromotionSource getInstance() {
        if (INSTANCE == null) {
            synchronized (RemotePromotionSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemotePromotionSource();
                }
            }
        }
        return INSTANCE;
    }

    public RemotePromotionSource() {
    }

    @Override
    public Observable<List<Promotion>> getPromotionList(User user) {
        return Observable.create(emitter -> {
            FirebaseFirestore.getInstance().collection("promotions").limit(PAGE_COUNT).get()
                    .addOnSuccessListener(snapshot -> {
                        List<Promotion> list = new ArrayList<>();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                Promotion p = new Promotion();
                                p.setTitle(doc.getString("title"));
                                p.setImageURL(doc.getString("imageUrl") != null ? doc.getString("imageUrl") : doc.getString("imgurl"));
                                p.setActionURL(doc.getString("link"));
                                list.add(p);
                            }
                        }
                        if (list.isEmpty()) {
                            list.addAll(createDefaultPromotions());
                        }
                        if (!emitter.isDisposed()) {
                            emitter.onNext(list);
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to load promotions from Firestore, using defaults: " + e.getMessage());
                        if (!emitter.isDisposed()) {
                            emitter.onNext(createDefaultPromotions());
                            emitter.onComplete();
                        }
                    });
        });
    }

    private List<Promotion> createDefaultPromotions() {
        List<Promotion> defaultPromotions = new ArrayList<>();
        defaultPromotions.add(new Promotion(1L, "https://picsum.photos/800/400?random=1", "https://github.com/boochlin06/FunnyVote", "歡迎來到 FunnyVote 投票平台！"));
        defaultPromotions.add(new Promotion(2L, "https://picsum.photos/800/400?random=2", "https://firebase.google.com", "全面串接 Firebase Firestore 後端架構"));
        return defaultPromotions;
    }

    @Override
    public void savePromotionList(List<Promotion> promotionList) {
        // Remote is read-only
    }
}
