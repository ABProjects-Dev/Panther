# Panther release R8 rules.
# Room and Hilt ship consumer rules inside their AARs; these explicit rules keep
# SQLite entity fields and Room's generated database constructor intact under R8 full mode.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
