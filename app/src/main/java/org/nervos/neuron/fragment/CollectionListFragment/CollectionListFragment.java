package org.nervos.neuron.fragment.CollectionListFragment;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.CollectionDetailActivity;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.item.CollectionItem;
import org.nervos.neuron.response.CollectionResponse;
import org.nervos.neuron.service.TokenService;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

public class CollectionListFragment extends NBaseFragment {

    private RecyclerView collectionRecycler;
    private CollectionAdapter adapter;
    private List<CollectionItem> collectionItemList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_collection;
    }

    @Override
    protected void initView() {
        super.initView();
        collectionRecycler = (RecyclerView) findViewById(R.id.collection_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    @Override
    protected void initAction() {
        super.initAction();

        swipeRefreshLayout.setOnRefreshListener(() -> getCollectionList());

        adapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(getActivity(), CollectionDetailActivity.class);
            intent.putExtra("collection", collectionItemList.get(position));
            startActivity(intent);
        });

        adapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(getActivity(), CollectionDetailActivity.class);
            intent.putExtra("collection", collectionItemList.get(position));
            startActivity(intent);
        });
    }

    @Override
    protected void initData() {
        super.initData();
        adapter = new CollectionAdapter(getContext(), collectionItemList);
        collectionRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        collectionRecycler.setAdapter(adapter);

        showProgressBar();
        getCollectionList();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        getCollectionList();
        collectionItemList.clear();
        adapter.refresh(collectionItemList);
        collectionRecycler.setVisibility(View.GONE);
    }

    private void getCollectionList() {
        TokenService.getCollectionList(getContext())
                .subscribe(new Subscriber<CollectionResponse>() {
                    @Override
                    public void onCompleted() {
                        swipeRefreshLayout.setRefreshing(false);
                        dismissProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                        dismissProgressBar();
                    }

                    @Override
                    public void onNext(CollectionResponse collectionResponse) {
                        collectionItemList = collectionResponse.assets;
                        collectionRecycler.setVisibility(View.VISIBLE);
                        adapter.refresh(collectionItemList);
                    }
                });
    }
}
