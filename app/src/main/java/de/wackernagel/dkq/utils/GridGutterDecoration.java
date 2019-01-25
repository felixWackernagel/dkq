package de.wackernagel.dkq.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.recyclerview.widget.RecyclerView;

public class GridGutterDecoration extends RecyclerView.ItemDecoration {

    private final int gutterSize;
    private final int spanCount;
    private boolean gutterOutsideTop;
    private boolean gutterOutsideBottom;
    private boolean gutterOutsideLeftAndRight;

    /**
     * Add some space between each item and the outside edges.
     *
     * @param gutterSize Space between each item and the outside edges
     * @param spanCount Number of grid columns
     */

    public GridGutterDecoration(@IntRange(from = 0) final int gutterSize, @IntRange(from = 1) final int spanCount) {
        this( gutterSize, spanCount, true, true );
    }

    /**
     * Add some space between each item and the outside edges.
     *
     * @param gutterSize Space between each item and the outside edges
     * @param spanCount Number of grid columns
     * @param outside Add a gutter on the outside edges of the grid
     */
    public GridGutterDecoration(@IntRange(from = 0) final int gutterSize, @IntRange(from = 1) final int spanCount, final boolean outside ) {
        this( gutterSize, spanCount, outside, outside );
    }

    /**
     * Add some space between each item and the outside edges.
     *
     * @param gutterSize Space between each item and the outside edges
     * @param spanCount Number of grid columns
     * @param gutterOutsideTopAndBottom Add a gutter on the first row top and last row bottom
     * @param gutterOutsideLeftAndRight Add a gutter on the first column left and last column right
     */
    public GridGutterDecoration(@IntRange(from = 0) final int gutterSize, @IntRange(from = 1) final int spanCount, final boolean gutterOutsideTopAndBottom, final boolean gutterOutsideLeftAndRight ) {
        this( gutterSize, spanCount, gutterOutsideTopAndBottom, gutterOutsideTopAndBottom, gutterOutsideLeftAndRight );
    }

    /**
     *
     * @param gutterSize Space between each item and the outside edges
     * @param spanCount Number of grid columns
     * @param gutterOutsideTop Add a gutter on the first row top
     * @param gutterOutsideBottom  Add a gutter on the last row bottom
     * @param gutterOutsideLeftAndRight Add a gutter on the first column left and last column right
     */
    public GridGutterDecoration(@IntRange(from = 0) final int gutterSize, @IntRange(from = 1) final int spanCount, final boolean gutterOutsideTop, final boolean gutterOutsideBottom, final boolean gutterOutsideLeftAndRight ) {
        this.gutterSize = gutterSize;
        this.spanCount = spanCount;
        this.gutterOutsideTop = gutterOutsideTop;
        this.gutterOutsideBottom = gutterOutsideBottom;
        this.gutterOutsideLeftAndRight = gutterOutsideLeftAndRight;
    }

    @Override
    public void getItemOffsets( Rect outRect, View view, RecyclerView parent, RecyclerView.State state ) {
        int itemPosition = parent.getChildAdapterPosition( view );
        int column = itemPosition % spanCount;
        int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
        int rowCount = ( itemCount / spanCount) + ( itemCount % spanCount == 0 ? 0 : 1 );
        boolean isFirstRow = itemPosition < spanCount;
        boolean isLastRow = itemPosition >= ( (rowCount - 1) * spanCount);

        if( gutterOutsideLeftAndRight ) {
            outRect.left = gutterSize - column * gutterSize / spanCount;
            outRect.right = (column + 1) * gutterSize / spanCount;
        } else {
            outRect.left = column * gutterSize / spanCount;
            outRect.right = gutterSize - (column + 1) * gutterSize / spanCount;
        }

        outRect.bottom = gutterSize;

        if( isFirstRow && gutterOutsideTop ) {
            outRect.top = gutterSize;
        }

        if( isLastRow && !gutterOutsideBottom ) {
            outRect.bottom = 0;
        }
    }
}