# Fix R.id.main Missing in Layouts

Add `android:id="@+id/main"` to the root layout element of all activity layouts to resolve the `findViewById(R.id.main)` error in Java activities.

## Proposed Changes

### Layouts

#### [MODIFY] [activity_login.xml](file:///C:/Users/yaraz/Documents/GitHub/yara-azevedo/myList/app/src/main/res/layout/activity_login.xml)
Add `android:id="@+id/main"` to the root `ConstraintLayout`.

#### [MODIFY] [activity_cadastro.xml](file:///C:/Users/yaraz/Documents/GitHub/yara-azevedo/myList/app/src/main/res/layout/activity_cadastro.xml)
Add `android:id="@+id/main"` to the root `ConstraintLayout`.

#### [MODIFY] [activity_main.xml](file:///C:/Users/yaraz/Documents/GitHub/yara-azevedo/myList/app/src/main/res/layout/activity_main.xml)
Add `android:id="@+id/main"` to the root `ConstraintLayout`.

#### [MODIFY] [activity_detalhe.xml](file:///C:/Users/yaraz/Documents/GitHub/yara-azevedo/myList/app/src/main/res/layout/activity_detalhe.xml)
Add `android:id="@+id/main"` to the root `ConstraintLayout`.

## Verification Plan

### Manual Verification
- Verify that the error in `LoginActivity.java`, `CadastroActivity.java`, `MainActivity.java`, and `DetalheActivity.java` disappears.
- Run the app and check if edge-to-edge padding is applied correctly.
