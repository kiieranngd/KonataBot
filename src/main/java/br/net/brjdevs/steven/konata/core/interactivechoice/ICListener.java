package br.net.brjdevs.steven.konata.core.interactivechoice;

public interface ICListener {
    void onValidResponse(InteractiveChoice choice, String response);
    void onInvalidResponse(InteractiveChoice choice, String response);
    void onTimeOut(InteractiveChoice choice);
}
