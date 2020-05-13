package com.example.coronadiagnosticapp.ui.fragments.oxymeter;

interface OxymeterThreadEventListener {
    void onFrame(int frameNumber);

    void onSuccess(Oxymeter oxymeter);

    void onFingerRemoved();

    void onInvalidData();

    void onStartWithNewOxymeter();
}
