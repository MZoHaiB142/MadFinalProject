$resDir = "e:\Mad\app\src\main\res"
$drawDir = "$resDir\drawable"
$valDir = "$resDir\values"

if (!(Test-Path $drawDir)) { New-Item -ItemType Directory -Path $drawDir | Out-Null }

$files = @{
    "bg_notification_badge.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#EF4444"/></shape>'
    "bg_icon_purple.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#8B5CF6"/></shape>'
    "bg_icon_blue.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#2563EB"/><corners android:radius="16dp"/></shape>'
    "bg_icon_green.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#16A34A"/><corners android:radius="16dp"/></shape>'
    "bg_icon_orange.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#F59E0B"/><corners android:radius="16dp"/></shape>'
    "bg_circle_arrow.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#EEF2FF"/></shape>'
    "bg_tag.xml" = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#FFFFFF"/><stroke android:width="1dp" android:color="#E5E7EB"/><corners android:radius="16dp"/></shape>'
    "ic_magic.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M7.5,5.6L10,7 8.6,4.5 10,2 7.5,3.4 5,2 6.4,4.5 5,7zM19.5,15.4L22,14l-1.4,2.5L22,19l-2.5,-1.4L17,19l1.4,-2.5L17,14zM22,2l-2.5,1.4L17,2l1.4,2.5L17,7l2.5,-1.4L22,7l-1.4,-2.5zm-7.6,5.2c-0.2,-0.2 -0.5,-0.2 -0.7,0l-9.9,9.9c-0.2,0.2 -0.2,0.5 0,0.7l3.5,3.5c0.2,0.2 0.5,0.2 0.7,0l9.9,-9.9c0.2,-0.2 0.2,-0.5 0,-0.7l-3.5,-3.5z"/></vector>'
    "ic_chat.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M20,2H4C2.9,2 2,2.9 2,4v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2zM6,9h12v2H6V9zm8,5H6v-2h8v2zm4,-6H6V6h12v2z"/></vector>'
    "ic_graduation.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M12,3L1,9l4,2.18v6L12,21l7,-3.82v-6l2.12,-1.15V17h2V9L12,3zm6.83,6.83L12,13.56L5.17,9.83 12,6.1l6.83,3.73zM12,18.72l-5,-2.73v-3.79l5,2.73 5,-2.73v3.79l-5,2.73z"/></vector>'
    "ic_book.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M18,2H6c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2zM6,4h5v8l-2.5,-1.5L6,12V4zm0,15l0,-2h12v2H6z"/></vector>'
    "ic_target.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zm0,18c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zm0,-14c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6zm0,10c-2.21,0 -4,-1.79 -4,-4s1.79,-4 4,-4 4,1.79 4,4 -1.79,4 -4,4zm0,-7c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z"/></vector>'
    "ic_notification.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.89,2 2,2zm6,-6v-5c0,-3.07 -1.64,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"/></vector>'
    "ic_arrow_right.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><path android:fillColor="#000000" android:pathData="M8.59,16.59L13.17,12 8.59,7.41 10,6l6,6 -6,6z"/></vector>'
    "img_test_prep_hero.xml" = '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="120dp" android:height="120dp" android:viewportWidth="120" android:viewportHeight="120"><path android:fillColor="#E5E7EB" android:pathData="M10,10h100v100H10z"/></vector>'
}

foreach ($key in $files.Keys) {
    $path = "$drawDir\$key"
    Set-Content -Path $path -Value $files[$key] -Encoding UTF8
}

$styles = '<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="TestTagStyle">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:background">@drawable/bg_tag</item>
        <item name="android:textColor">#4B5563</item>
        <item name="android:textSize">10sp</item>
        <item name="android:paddingHorizontal">12dp</item>
        <item name="android:paddingVertical">4dp</item>
        <item name="android:layout_marginEnd">8dp</item>
    </style>
</resources>'

Set-Content -Path "$valDir\test_prep_styles.xml" -Value $styles -Encoding UTF8
