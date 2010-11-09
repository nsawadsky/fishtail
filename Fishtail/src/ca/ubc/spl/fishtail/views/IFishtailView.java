package ca.ubc.spl.fishtail.views;

import ca.ubc.spl.fishtail.IKeywordListener;

public abstract interface IFishtailView extends IKeywordListener
{
  public abstract void refreshView();

  public abstract ViewType getType();

  public abstract void openSelectedHit();

  public static enum ViewType
  {
    View, Inplace;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.views.IFishtailView
 * JD-Core Version:    0.6.0
 */