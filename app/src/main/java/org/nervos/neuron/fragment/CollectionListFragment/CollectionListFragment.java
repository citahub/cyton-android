package org.nervos.neuron.fragment.CollectionListFragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.nervos.neuron.R;
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

        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCollectionList();
            }
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
                    adapter.refresh(collectionItemList);
                }
            });
    }
}
