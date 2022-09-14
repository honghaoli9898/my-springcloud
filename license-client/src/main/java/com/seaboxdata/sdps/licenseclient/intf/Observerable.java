package com.seaboxdata.sdps.licenseclient.intf;

//被观察者
public interface Observerable{

    void registerObserver(Observer o);

    void removeObserver(Observer o);

    void notifyObserver();

}
