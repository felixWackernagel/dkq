package de.wackernagel.dkq.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.R;
import de.wackernagel.dkq.room.message.Message;
import de.wackernagel.dkq.room.message.MessageListItem;
import de.wackernagel.dkq.ui.widgets.BadgedFourThreeImageView;
import de.wackernagel.dkq.ui.widgets.BadgedView;
import de.wackernagel.dkq.utils.DeviceUtils;
import de.wackernagel.dkq.utils.GlideUtils;
import de.wackernagel.dkq.utils.GridGutterDecoration;
import de.wackernagel.dkq.utils.SectionItemDecoration;
import de.wackernagel.dkq.utils.SlideUpAlphaAnimator;
import de.wackernagel.dkq.viewmodels.MessagesViewModel;
import de.wackernagel.dkq.webservice.Status;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MessagesListFragment extends Fragment {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private MessageAdapter adapter;

    static MessagesListFragment newInstance() {
        final MessagesListFragment fragment = new MessagesListFragment();
        fragment.setArguments( Bundle.EMPTY );
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated( @NonNull final View view, @Nullable final Bundle savedInstanceState) {
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

            adapter = new MessagesListFragment.MessageAdapter( requireContext(), new MessageItemCallback() );
            recyclerView.setHasFixedSize( true );
            recyclerView.setItemAnimator( animator );
            recyclerView.setAdapter( adapter );
            recyclerView.addItemDecoration( new GridGutterDecoration(
                    DeviceUtils.dpToPx(16, requireContext() ),
                    1,
                    false,
                    true,
                    true ) );
            recyclerView.addItemDecoration( new SectionItemDecoration( requireContext(),false, new SectionItemDecoration.SectionCallback() {
                @Override
                public boolean isSection(int position) {
                    return position < 1;
                }

                @Override
                public CharSequence getSectionHeader(int position) {
                    if( position == 0 ) {
                        final int count = adapter.getItemCount();
                        return getResources().getQuantityString( R.plurals.messages_section, count, count );
                    }
                    return "";
                }
            }) );

            final MessagesViewModel viewModel = new ViewModelProvider( this, viewModelFactory ).get(MessagesViewModel.class);
            viewModel.loadMessages().observe(getViewLifecycleOwner(), messages -> {
                if( messages != null ) {
                    DkqLog.i("MessagesListFragment", "Status=" + messages.status + ", Items=" + (messages.data != null ? messages.data.size() : "null" ) + ", Message=" + messages.message );

                    progressBar.setVisibility( messages.status == Status.LOADING ? VISIBLE : GONE );
                    emptyView.setVisibility( messages.status != Status.LOADING && ( messages.data == null || messages.data.isEmpty() ) ? VISIBLE : GONE );

                    adapter.submitList( messages.data );
                }
            });
        }
    }

    static class MessageItemCallback extends DiffUtil.ItemCallback<MessageListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull MessageListItem oldItem, @NonNull MessageListItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MessageListItem oldItem, @NonNull MessageListItem newItem) {
            return TextUtils.equals( oldItem.getImage(), newItem.getImage() )
                    && TextUtils.equals( oldItem.getTitle(), newItem.getTitle() )
                    && TextUtils.equals( oldItem.getContent(), newItem.getContent() )
                    && oldItem.isRead() == newItem.isRead()
                    && Objects.equals( oldItem.getType(), newItem.getType() );
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private BadgedView badge;
        private BadgedFourThreeImageView image;
        private TextView title;
        private TextView content;

        MessageViewHolder( final View itemView ) {
            super(itemView);
            badge = itemView.findViewById( R.id.badge );
            image = itemView.findViewById( R.id.image );
            title = itemView.findViewById( R.id.title );
            content = itemView.findViewById( R.id.content );
        }
    }

    static class MessageAdapter extends ListAdapter<MessageListItem, MessageViewHolder> {
        private final int width;
        private final int height;

        MessageAdapter( final Context context, @NonNull DiffUtil.ItemCallback<MessageListItem> diffCallback) {
            super(diffCallback);
            setHasStableIds( true );

            final int margin = DeviceUtils.dpToPx(16, context );
            final int deviceWidth = DeviceUtils.getDeviceWidth( context );
            width = deviceWidth - ( margin * 2 );
            height = GlideUtils.fourThreeHeightOf( width );
        }

        @Override
        public long getItemId(int position) {
            return getItem( position ).getId();
        }

        @Override
        public int getItemViewType(int position) {
            switch( getItem( position ).getType() ) {
                case UPDATE_LOG:
                    return R.layout.item_message_update_log;

                case ARTICLE:
                default:
                    return R.layout.item_message_article;
            }
        }

        @NonNull
        @Override
        public MessagesListFragment.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessagesListFragment.MessageViewHolder( LayoutInflater.from( parent.getContext() ).inflate( viewType, parent, false ) );
        }

        @Override
        public void onBindViewHolder( @NonNull final MessagesListFragment.MessageViewHolder holder, final int position) {
            if( position != RecyclerView.NO_POSITION ) {
                final MessageListItem message = getItem( position);

                holder.title.setText( message.getTitle() );
                if( Message.Type.ARTICLE.equals( message.getType() ) ) {
                    GlideUtils.loadImage( holder.image, message.getImage(), message.getVersion(), width, height );
                    holder.image.drawBadge(!message.isRead());
                    holder.content.setText( message.getContent() );
                } else {
                    holder.badge.drawBadge(!message.isRead());
                }

                holder.itemView.setOnClickListener(view -> {
                    final Context context = holder.itemView.getContext();
                    context.startActivity( MessageDetailsActivity.createLaunchIntent( context, message.getId(), message.getNumber() ) );
                });
            }
        }
    }
}
