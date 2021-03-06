package de.wackernagel.dkq.utils;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SlideUpAlphaAnimator extends BaseItemAnimator<SlideUpAlphaAnimator> {

    // Add animations

    @Override
    public void addAnimationPrepare(RecyclerView.ViewHolder holder) {
        holder.itemView.setTranslationY(holder.itemView.getHeight());
        holder.itemView.setAlpha(0);
    }

    @Override
    public ViewPropertyAnimatorCompat addAnimation(RecyclerView.ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
            .translationY(0)
            .alpha(1)
            .setStartDelay( 50 * holder.getAdapterPosition() )
            .setDuration(getMoveDuration())
            .setInterpolator(getInterpolator());
    }

    @Override
    public void addAnimationCleanup(RecyclerView.ViewHolder holder) {
        holder.itemView.setTranslationY(0);
        holder.itemView.setAlpha(1);
    }

    // change animations

    @Override
    public ViewPropertyAnimatorCompat changeOldAnimation(RecyclerView.ViewHolder holder, ChangeInfo changeInfo) {
        return ViewCompat.animate(holder.itemView)
                .setDuration(getChangeDuration())
                .alpha(0)
                .translationX(changeInfo.toX - changeInfo.fromX)
                .translationY(changeInfo.toY - changeInfo.fromY)
                .setInterpolator(getInterpolator());
    }

    @Override
    public ViewPropertyAnimatorCompat changeNewAnimation(RecyclerView.ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
                .translationX(0)
                .translationY(0)
                .setDuration(getChangeDuration())
                .alpha(1)
                .setInterpolator(getInterpolator());
    }

    @Override
    public void changeAnimationCleanup(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(1);
    }

    // remove animations

    @Override
    public ViewPropertyAnimatorCompat removeAnimation(RecyclerView.ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
            .setDuration(getRemoveDuration())
            .alpha(0)
            .translationY(holder.itemView.getHeight())
            .setInterpolator(getInterpolator());
    }

    @Override
    public void removeAnimationCleanup(RecyclerView.ViewHolder holder) {
        holder.itemView.setTranslationY(0);
        holder.itemView.setAlpha(1);
    }

    // animation delays

    @Override
    public long getAddDelay(long remove, long move, long change) {
        return 0;
    }

    @Override
    public long getRemoveDelay(long remove, long move, long change) {
        return 0;
    }
}