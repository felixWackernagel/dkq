package de.wackernagel.dkq.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.AndroidSupportInjection;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.entities.MessageListItem;
import de.wackernagel.dkq.ui.widgets.BadgedFourThreeImageView;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SectionItemDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.MessagesViewModel;
import de.wackernagel.dkq.webservice.Resource;

public class MessagesListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private MessageAdapter adapter;

    static MessagesListFragment newInstance() {
        final MessagesListFragment fragment = new MessagesListFragment();
        fragment.setArguments( new Bundle(0) );
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById( R.id.recyclerView );
        emptyView = view.findViewById( R.id.emptyView );
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);

        if( recyclerView != null ) {
            final SlideUpAlphaAnimator animator = new SlideUpAlphaAnimator().withInterpolator( new FastOutSlowInInterpolator() );
            animator.setAddDuration( 400 );
            animator.setChangeDuration( 400 );
            animator.setMoveDuration( 400 );
            animator.setRemoveDuration( 400 );

            final int columnCount = DeviceUtils.isPortraitMode( getContext() ) ? 1 : 2;
            adapter = new MessagesListFragment.MessageAdapter( new MessageItemCallback() );
            recyclerView.setLayoutManager( new GridLayoutManager( getContext(), columnCount ) );
            recyclerView.setHasFixedSize( true );
            recyclerView.setItemAnimator( animator );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, getContext()),
                    columnCount,
                    false,
                    true,
                    true ) );
            recyclerView.addItemDecoration( new SectionItemDecoration( getContext(),false, new SectionItemDecoration.SectionCallback() {
                @Override
                public boolean isSection(int position) {
                    return position < columnCount;
                }

                @Override
                public CharSequence getSectionHeader(int position) {
                    if( position == 0 ) {
                        return getString( R.string.messages_section, adapter.getItemCount() );
                    }
                    return "";
                }
            }) );

            final MessagesViewModel viewModel = ViewModelProviders.of( this, viewModelFactory ).get(MessagesViewModel.class);
            viewModel.loadMessages().observe(this, new Observer<Resource<List<MessageListItem>>>() {
                @Override
                public void onChanged(@Nullable Resource<List<MessageListItem>> messages) {
                    if( messages != null ) {
                        Log.i("dkq", "Status=" + messages.status + ", Items=" + (messages.data != null ? messages.data.size() : "null" ));
                        switch (messages.status) {
                            case LOADING:
                                progressBar.setVisibility( View.VISIBLE );
                                emptyView.setVisibility( View.GONE );
                                break;
                            case ERROR:
                            case SUCCESS:
                                progressBar.setVisibility( View.GONE );
                                emptyView.setVisibility( messages.data != null && messages.data.size() > 0 ? View.GONE : View.VISIBLE );
                                break;
                        }
                        adapter.submitList( messages.data );
                    }
                }
            });
        }
    }

    static class MessageItemCallback extends DiffUtil.ItemCallback<MessageListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull MessageListItem oldItem, @NonNull MessageListItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MessageListItem oldItem, @NonNull MessageListItem newItem) {
            return TextUtils.equals( oldItem.image, newItem.image )
                    && TextUtils.equals( oldItem.title, newItem.title )
                    && TextUtils.equals( oldItem.content, newItem.content )
                    && oldItem.read == newItem.read;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private BadgedFourThreeImageView image;
        private TextView title;
        private TextView content;

        MessageViewHolder( final View itemView ) {
            super(itemView);
            image = itemView.findViewById( R.id.image );
            title = itemView.findViewById( R.id.title );
            content = itemView.findViewById( R.id.content );
        }
    }

    static class MessageAdapter extends ListAdapter<MessageListItem, MessageViewHolder> {
        MessageAdapter(@NonNull DiffUtil.ItemCallback<MessageListItem> diffCallback) {
            super(diffCallback);
        }

        @Override
        public MessagesListFragment.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessagesListFragment.MessageViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_message, parent, false ) );
        }

        @Override
        public void onBindViewHolder( final MessagesListFragment.MessageViewHolder holder, final int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final MessageListItem message = getItem( position);

                GlideUtils.loadImage( holder.image, message.image );
                holder.title.setText( message.title );
                holder.content.setText( message.content );
                holder.image.drawBadge( message.read == 0 );

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Context context = holder.itemView.getContext();
                        context.startActivity( MessageDetailsActivity.createLaunchIntent( context, message.id, message.number ) );
                    }
                });
            }
        }
    }
}
