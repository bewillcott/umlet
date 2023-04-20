
package com.baselet.element.elementnew;

import com.baselet.control.enums.ElementId;
import com.baselet.element.NewGridElement;
import com.baselet.element.elementnew.plot.PlotGrid;
import com.baselet.element.elementnew.uml.Class;
import com.baselet.element.elementnew.uml.Package;
import com.baselet.element.elementnew.uml.*;
import com.baselet.element.relation.Relation;

public abstract class ElementFactory
{
    protected ElementFactory()
    {
    }

    protected static NewGridElement createAssociatedGridElement(ElementId id)
    {
        switch (id)
        {
            case PlotGrid:
                return new PlotGrid();
            case Relation:
                return new Relation();
            case Text:
                return new Text();
            case UMLActor:
                return new Actor();
            case UMLClass:
                return new Class();
            case UMLDeployment:
                return new Deployment();
            case UMLFrame:
                return new Frame();
            case UMLGeneric:
                return new Generic();
            case UMLInterface:
                return new Interface();
            case UMLNote:
                return new Note();
            case UMLObject:
                return new ActivityObject();
            case UMLPackage:
                return new Package();
            case UMLSpecialState:
                return new SpecialState();
            case UMLState:
                return new State();
            case UMLSyncBarHorizontal:
                return new SyncBarHorizontal();
            case UMLSyncBarVertical:
                return new SyncBarVertical();
            case UMLTimer:
                return new Timer();
            case UMLUseCase:
                return new UseCase();
            case UMLHierarchy:
                return new Hierarchy();
            case UMLSequenceAllInOne:
                return new SequenceAllInOne();
            default:
                throw new RuntimeException("Unknown class id: " + id);
        }
    }
}
