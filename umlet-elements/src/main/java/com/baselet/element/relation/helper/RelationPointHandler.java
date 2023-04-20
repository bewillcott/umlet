
package com.baselet.element.relation.helper;

import com.baselet.control.SharedUtils;
import com.baselet.control.basics.geom.Line;
import com.baselet.control.basics.geom.Point;
import com.baselet.control.basics.geom.PointDouble;
import com.baselet.control.basics.geom.Rectangle;
import com.baselet.diagram.draw.DrawHandler;
import com.baselet.element.sticking.PointChange;
import com.baselet.element.sticking.PointDoubleIndexed;
import java.util.*;

public class RelationPointHandler implements ResizableObject
{

    /*
     * Points of this relation (point of origin is the upper left corner of the
     * relation element (not the drawpanel!))
     */
    private RelationPointList points = new RelationPointList();

    private final RelationPointHolder relation;

    private PointDoubleIndexed relationPointOfCurrentDrag = null;

    public RelationPointHandler(RelationPointHolder relation, RelationPointList points)
    {
        super();
        this.relation = relation;
        this.points = points;
    }

    public void drawCirclesAndDragBox(DrawHandler drawer)
    {
        for (RelationPoint p : points.getPointHolders())
        {
            drawer.drawCircle(p.getPoint().getX(), p.getPoint().getY(), RelationPointConstants.POINT_SELECTION_RADIUS);
        }
        drawer.drawRectangle(getDragBox());
    }
    // DRAW METHODS

    public void drawLinesBetweenPoints(DrawHandler drawer, boolean shortFirstLine, boolean shortLastLine)
    {
        List<Line> lines = points.getRelationPointLines();
        for (int i = 0; i < lines.size(); i++)
        {
            Line lineToDraw = lines.get(i);
            if (i == 0 && shortFirstLine)
            {
                lineToDraw = lineToDraw.getShorterVersion(true, RelationDrawer.SHORTEN_IF_ARROW);
            } else if (i == lines.size() - 1 && shortLastLine)
            {
                lineToDraw = lineToDraw.getShorterVersion(false, RelationDrawer.SHORTEN_IF_ARROW);
            }
            drawer.drawLine(lineToDraw);
        }
    }

    public void drawSelectionSpace(DrawHandler drawer)
    {
        for (RelationPoint rp : points.getPointHolders())
        {
            drawer.drawRectangle(rp.getSizeAbsolute());
        }
    }

    public Rectangle getDragBox()
    {
        return points.getDragBox();
    }

    // HELPER METHODS
    public Line getFirstLine()
    {
        return points.getFirstLine();
    }

    public Line getLastLine()
    {
        return points.getLastLine();
    }

    public Line getLineContaining(PointDouble point)
    {
        for (Line line : points.getRelationPointLines())
        {
            double distanceToPoint = line.getDistanceToPoint(point);
            if (distanceToPoint < RelationPointConstants.NEW_POINT_DISTANCE)
            {
                return line;
            }
        }
        return null;
    }

    public Line getMiddleLine()
    {
        return points.getMiddleLine();
    }

    public PointDouble getRelationCenter()
    {
        return points.getRelationCenter();
    }

    public List<RelationPoint> getRelationPoints()
    {
        return points.getPointHolders();
    }

    public RelationSelection getSelection(Point point)
    {
        if (isPointOverDragBox(point))
        {
            return RelationSelection.DRAG_BOX;
        } else if (RelationPointHandlerUtils.getRelationPointContaining(point, points) != null)
        {
            return RelationSelection.RELATION_POINT;
        } else if (getLineContaining(point.toPointDouble()) != null)
        {
            return RelationSelection.LINE;
        } else
        {
            return RelationSelection.NOTHING;
        }
    }

    /*
     * this method is basically the same as {@link #getSelection(Point)}, but
     * also applies changes to the relationpoints
     * (the order of checks is the same, but they do different things, therefore
     * they are separated)
     */
    public RelationSelection getSelectionAndMovePointsIfNecessary(Point point, Integer diffX, Integer diffY, boolean firstDrag)
    {
        // Special case: if this is not the first drag and a relation-point is currently dragged, it has preference
        // Necessary to avoid changing the currently moved point if moving over another point and to avoid losing the current point if it's a new line point and the mouse is dragged very fast
        if (!firstDrag && relationPointOfCurrentDrag != null)
        {
            relationPointOfCurrentDrag = movePointAndResizeRectangle(relationPointOfCurrentDrag, diffX, diffY);
            return RelationSelection.RELATION_POINT;
        }
        // If the special case doesn't apply, forget the relationPointOfFirstDrag, because its a new first drag
        relationPointOfCurrentDrag = null;
        if (isPointOverDragBox(point))
        {
            return RelationSelection.DRAG_BOX;
        }
        PointDoubleIndexed pointOverRelationPoint = RelationPointHandlerUtils.getRelationPointContaining(point, points);
        if (pointOverRelationPoint != null)
        {
            relationPointOfCurrentDrag = movePointAndResizeRectangle(pointOverRelationPoint, diffX, diffY);
            return RelationSelection.RELATION_POINT;
        }
        Line lineOnPoint = getLineContaining(point.toPointDouble());
        if (lineOnPoint != null)
        {
            relationPointOfCurrentDrag = points.addPointOnLine(lineOnPoint, SharedUtils.realignToGridRoundToNearest(false, point.x), SharedUtils.realignToGridRoundToNearest(false, point.y));
            relationPointOfCurrentDrag = movePointAndResizeRectangle(relationPointOfCurrentDrag, diffX, diffY);
            return RelationSelection.LINE;
        }
        return RelationSelection.NOTHING;
    }

    public Collection<PointDoubleIndexed> getStickablePoints()
    {
        return points.getStickablePoints();
    }

    public List<PointDoubleIndexed> movePointAndResizeRectangle(List<PointChange> changedPoints)
    {
        points.applyChangesToPoints(changedPoints);
        resizeRectAndReposPoints();
        List<PointDoubleIndexed> updatedChangedPoint = new ArrayList<>();
        for (PointChange c : changedPoints)
        {
            updatedChangedPoint.add(points.get(c.getIndex()));
        }
        return updatedChangedPoint;
    }

    public boolean removeRelationPointIfOnLineBetweenNeighbourPoints()
    {
        return points.removeRelationPointIfOnLineBetweenNeighbourPoints();
    }

    @Override
    public void resetPointMinSize(int index)
    {
        points.setSize(index, RelationPoint.DEFAULT_SIZE);
    }

    /*
     * resets all textbox indexes except those which are contained in the
     * excludedList
     */
    public void resetTextBoxIndexesExcept(Set<Integer> excludedList)
    {
        Set<Integer> unusedTextBoxIndexes = new HashSet<>(points.getTextBoxIndexes());
        unusedTextBoxIndexes.removeAll(excludedList);
        for (Integer index : unusedTextBoxIndexes)
        {
            points.removeTextBox(index);
        }
    }

    public void resizeRectAndReposPoints()
    {
        // now rebuild width and height of the relation, based on the new positions of the relation-points
        Rectangle newRect = RelationPointHandlerUtils.calculateRelationRectangleBasedOnPoints(relation.getRectangle().getUpperLeftCorner(), relation.getGridSize(), points);
        relation.setRectangle(newRect);

        // move relation points to their new position (their position is relative to the relation-position)
        points.moveRelationPointsAndTextSpacesByToUpperLeftCorner();
    }

    @Override
    public void setPointMinSize(int index, Rectangle size)
    {
        Rectangle size1 = SharedUtils.realignToGrid(size, true);
        points.setSize(index, size1);
    }

    public void setTextBox(int index, Rectangle size)
    {
        points.setTextBox(index, size);
    }

    public String toAdditionalAttributesString()
    {
        return points.toAdditionalAttributesString();
    }

    @Override
    public String toString()
    {
        return points.toString();
    }

    private boolean isPointOverDragBox(Point point)
    {
        return getDragBox().contains(point);
    }

    private PointDoubleIndexed movePointAndResizeRectangle(PointDoubleIndexed point, Integer diffX, Integer diffY)
    {
        return movePointAndResizeRectangle(Arrays.asList(new PointChange(point.getIndex(), diffX, diffY))).get(0);
    }
}
