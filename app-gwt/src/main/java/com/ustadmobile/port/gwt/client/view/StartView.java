package com.ustadmobile.port.gwt.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class StartView extends Composite implements IsWidget{
	
	
	HorizontalPanel panelContainer;
	Label label1;
	Button gotoButton;
	private Presenter presenter;
	
	public StartView(){
		panelContainer = new HorizontalPanel();
		label1 = new Label();
		gotoButton = new Button("SecondView");
		
		panelContainer.add(label1);
		panelContainer.add(gotoButton);
	}
	
	public Presenter getPresenter(){
		return presenter;
	}
	public void setPresenter(Presenter presenter){
		this.presenter = presenter;
	}

	public void setMessage(String message){
		label1.setText(message);
	}
	
	@Override
	public Widget asWidget() {
		return panelContainer;
	}
	
	public interface Presenter{
	       public void goTo(Place place);
	       public void setMessage(String message);
	}

}
