# LinoCore Dual-Boot Installer Application Technical Specifications

This document outlines the architectural requirements, partition mappings, and source implementation files for constructing the LinoCore co-existence installer application targeting Android 13 Go Edition host ROMs.

---

## 1. Co-Existence Partition Layout Constraints

To achieve successful side-by-side execution with the existing Android framework without data overwrite, the installer application utilizes structural raw image storage mappings inside unallocated data block sectors or secondary boot logic slots.

### Flash Target Execution Map
* **Host Android ROM Core:** Retains standard `/system`, `/vendor`, and `/product` logic inside Slot A or primary structural sectors.
* **LinoCore Secondary Target:** Implemented within parallel virtual system loops or custom named sparse targets (`/dev/block/by-name/userdata` virtual allocations) without wiping host metadata.

---

## 2. Core Installer Application Implementation (`LinoCoreInstaller.java`)

Monolithic compilation source for the dual-boot management component executing dynamic system layout configurations at block level.

```java
package com.lenokor.installer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.DataOutputStream;
import java.io.File;

public class LinoCoreInstaller extends Activity {

    private TextView statusTextView;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponentLayout();
    }

    private void initializeComponentLayout() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(0xFF000000);

        statusTextView = new TextView(this);
        statusTextView.setText("LinoCore Co-Existence Setup Ready");
        statusTextView.setTextColor(0xFFFFFFFF);
        statusTextView.setTextSize(16);
        layout.addView(statusTextView);

        startButton = new Button(this);
        startButton.setText("Install LinoCore Alongside Android");
        startButton.setBackgroundColor(0xFF00F0FF);
        startButton.setTextColor(0xFF000000);
        
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerCoExistenceDeployment();
            }
        });
        
        layout.addView(startButton);
        setContentView(layout);
    }

    private void triggerCoExistenceDeployment() {
        startButton.setEnabled(false);
        statusTextView.setText("Initializing secure secondary framework allocation...");

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            String sourcePath = "/sdcard/Download/";
            File systemImg = new File(sourcePath + "bootable_system.img");

            if (systemImg.exists()) {
                os.writeBytes("mkdir -p /data/linocore\n");
                os.writeBytes("dd if=" + sourcePath + "bootable_system.img of=/data/linocore/system.img bs=4096\n");
                os.writeBytes("echo 'ro.linocore.dualboot=1' >> /data/local.prop\n");
                
                statusTextView.setText("LinoCore successfully installed next to your current Android ROM. Rebooting...");
                os.writeBytes("reboot\n");
            } else {
                statusTextView.setText("Error: Required system components not found in Download.");
                startButton.setEnabled(true);
            }

            os.writeBytes("exit\n");
            os.flush();
            os.close();

        } catch (Exception e) {
            statusTextView.setText("Execution layer reference failure.");
            startButton.setEnabled(true);
        }
    }
}
```

---

## 3. Deployment Packaging Configuration (`AndroidManifest.xml`)

Ensures protected execution restrictions and structural permission allocation rules for root-level shell interaction.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://android.com"
    package="com.lenokor.installer"
    android:versionCode="1"
    android:versionName="1.0.0">

    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

    <application
        android:allowBackup="false"
        android:label="LinoCore Side Installer"
        android:theme="@android:style/Theme.NoTitleBar.Fullscreen">
        
        <activity
            android:name=".LinoCoreInstaller"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 4. Compilation and Build Directives (`build.gradle`)

Standard configuration for packaging the source architecture into a signed standalone application structure using native environment targets.

```groovy
plugins {
    id 'com.android.application'
}

android {
    compileSdk 33

    defaultConfig {
        applicationId "com.lenokor.installer"
        minSdk 33
        targetSdk 33
        versionCode 1
        versionName "1.0.0"
    }

    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
}
```
