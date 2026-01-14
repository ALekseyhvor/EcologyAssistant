package space.hvoal.ecologyassistant.app;

import android.app.Application;

import ru.dgis.sdk.DGis;
import ru.dgis.sdk.PersonalDataCollectionConsent;
import ru.dgis.sdk.map.GlobalMapOptions;

import ru.dgis.sdk.platform.HttpOptions;
import ru.dgis.sdk.platform.KeyFromAsset;
import ru.dgis.sdk.platform.KeySource;
import ru.dgis.sdk.platform.LogOptions;
import ru.dgis.sdk.platform.VendorConfig;
import ru.dgis.sdk.platform.StorageOptions;

public class EcologyAssistantApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DGis.initialize(
                this,
                new HttpOptions(),
                new LogOptions(),
                new VendorConfig(),
                new KeySource(new KeyFromAsset("dgissdk.key")),
                PersonalDataCollectionConsent.GRANTED,
                null, // platformHttpClient
                null, // platformAudioDriver
                new GlobalMapOptions(),
                new StorageOptions()
        );

    }
}
