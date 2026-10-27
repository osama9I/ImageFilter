# Image Filter

Eine Java-25-Desktop-Anwendung mit Swing. Keine zusätzlichen Bibliotheken und kein Maven/Gradle nötig. Die vollständige Aufgabenstellung und das Beispielprogramm des Dozenten fehlen im Projektordner; umgesetzt sind die angegebenen Pflichtfunktionen. Toonify und Bildexport sind nicht enthalten.

## In IntelliJ starten

1. Den Ordner `FilterImage` als Projekt öffnen.
2. Unter **File → Project Structure → Project** das SDK **ms-25** (Java 25) und Sprachlevel **25** auswählen. Das Projekt ist bereits so eingestellt. Das Modul übernimmt das Projekt-SDK.
3. Falls das SDK fehlt: **Add SDK → JDK** und den installierten Java-25-Ordner wählen. Auf diesem Rechner ist das `C:\Users\osama\.jdks\ms-25.0.4.1`.
4. `src/Main.java` öffnen und das grüne Dreieck neben `main` anklicken. Das Fenster **Image Filter** erscheint.

## Benutzen

1. **Bild öffnen …** anklicken und ein JPG/JPEG oder PNG auswählen. Links neben der Ergebnisanzeige bleibt das Original sichtbar. Transparenz wird auf einem Schachbrett dargestellt.
2. Links oben einen Filtertyp auswählen und **Hinzufügen** anklicken. Derselbe Filter darf mehrfach vorkommen.
3. Einen Eintrag in der Liste auswählen. Seine Regler erscheinen darunter; Änderungen aktualisieren das Ergebnis automatisch nach einer kurzen Pause.
4. Mit **Nach oben**, **Nach unten** und **Entfernen** die Liste bearbeiten. Berechnet wird von oben nach unten. Jeder Eintrag behält seine eigenen Einstellungen.
5. Ohne Filter entspricht das Ergebnis dem Original. Beim Öffnen eines anderen Bildes bleibt die Filterliste erhalten und wird auf das neue Bild angewendet.

Große Bilder werden nur für die Anzeige skaliert; gerechnet wird mit der Originalauflösung. Die Berechnung läuft im Hintergrund und veraltete Berechnungen werden abgebrochen. Sehr große Bilder und lange Filterlisten können viel Arbeitsspeicher und Zeit benötigen.

| Filter | Einstellung und Wirkung |
| --- | --- |
| Gaussian Blur | Radius 0–10; Gauß-gewichtete Nachbarpixel machen Übergänge weich. 0 deaktiviert den Effekt. |
| Binarization / Thresholding | Schwellwert 0–255; gewichtete Helligkeit ab dem Schwellwert wird weiß, darunter schwarz. |
| Edge Detection | Sobel-Kantenerkennung; Stärke 0–300 %. Helligkeitsänderungen werden als helle Kanten auf Schwarz dargestellt. |
| Sharpening | Stärke 0–300 %; Differenz zu den vier direkten Nachbarn betont Übergänge. 0 deaktiviert den Effekt. |
| Invert | Kehrt RGB-Farbwerte mit `255 - Wert` um; keine Einstellungen. |
| Brightness / Contrast | Helligkeit −255 bis 255, Kontrast 0–300 % um den Mittelpunkt 128. Neutral: 0 und 100 %. |

Farbwerte werden auf 0–255 begrenzt. Jeder Filter erhält die Alpha-Werte der Pixel. Nachbarschaftsfilter wiederholen am Bildrand den nächstgelegenen Pixel. Gauß berücksichtigt die Transparenz beim Gewichten der Farben.

## Code verstehen

- `src/Main.java`: startet Swing im Ereignisthread.
- `src/ui/ImageFilterFrame.java`: Fenster, Filterliste, Regler, Dateiauswahl und Hintergrundaufgaben mit `SwingWorker`. Eine Versionsnummer verhindert, dass ein älteres Ergebnis ein neueres ersetzt.
- `src/ui/ImagePanel.java`: zeichnet Bilder passend zur verfügbaren Fläche und zeigt Transparenz an.
- `src/imaging/ImageFiles.java`: lädt und prüft das tatsächliche Dateiformat; ungültige Dateien führen zu einer Fehlermeldung.
- `src/filters/ImageFilter.java`: gemeinsame Schnittstelle für Filter.
- `src/filters/FilterType.java`: sechs verfügbare Filtertypen.
- `src/filters/FilterStep.java`: Typ und Einstellungen eines Listeneintrags. Für Hintergrundberechnungen werden unabhängige Kopien erstellt.
- `src/filters/ImageFilters.java`: Pixelberechnungen und Anwendung der gesamten Filterliste. Die Filter ändern das Original nicht.
- `tests/FilterTests.java`: selbstständiges Testprogramm ohne zusätzliche Testbibliothek. Der Ordner `tests` ist in IntelliJ als Testquellordner eingetragen.

Der Datenfluss lautet: Originalbild → erster Filter → zweiter Filter → weitere Filter → Ergebnisanzeige. Bei jeder Änderung beginnt die Berechnung wieder beim Original.

## Tests

In IntelliJ `tests/FilterTests.java` öffnen und dessen `main` starten. Optional unter **Run → Edit Configurations → Program arguments** `--gui` eintragen: Dann wird zusätzlich das Fenster kurz geöffnet und wieder geschlossen.

Aus dem Projektordner in PowerShell:

```powershell
$jdk25 = 'C:\Users\osama\.jdks\ms-25.0.4.1\bin'
New-Item -ItemType Directory -Force out/classes | Out-Null
$sources = @(Get-ChildItem src,tests -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& "$jdk25\javac.exe" -encoding UTF-8 --release 25 -d out/classes $sources
& "$jdk25\java.exe" '-Djava.awt.headless=true' -cp out/classes FilterTests
& "$jdk25\java.exe" -cp out/classes FilterTests --gui
& "$jdk25\java.exe" -cp out/classes Main
```

Durchgeführt mit Java 25.0.4.1: Kompilierung erfolgreich; **288 Prüfungen ohne Fenster**, **289 Prüfungen einschließlich Fensterstart** bestanden. Geprüft wurden bekannte Pixelwerte, neutrale Einstellungen, Begrenzung, Schwellwertgrenzen, Gauß, Sobel, Schärfung, Transparenz, kleine Bilder, Originalschutz, Filterreihenfolge, unabhängige Einstellungen, Abbruch, JPG-/PNG-Laden, ungültige/andere Formate und das Zeichnen der Bildanzeige.

Der Fensterstart wurde automatisch geprüft. Die vollständige Bedienung per Maus, der native Dateidialog und das visuelle Ergebnis mit eigenen Fotos wurden nicht manuell geprüft. Dafür in IntelliJ je ein JPG und transparentes PNG öffnen, Filter hinzufügen/entfernen/verschieben, Regler verändern und das Fenster vergrößern. Helligkeit vor Thresholding und danach in umgekehrter Reihenfolge ausprobieren; die Ergebnisse sollten unterschiedlich sein. Auch eine ungültige Datei mit `.png`-Endung auswählen und die Fehlermeldung prüfen.

## شرح بالعربية

البرنامج يستخدم **Java 25** وSwing دون مكتبات إضافية. افتح المشروع في IntelliJ، وتأكد أن SDK هو `ms-25` ومستوى اللغة 25، ثم افتح `src/Main.java` واضغط زر التشغيل الأخضر بجانب `main`.

اضغط **Bild öffnen …** لفتح صورة JPG أو PNG. اختر فلترًا من القائمة واضغط **Hinzufügen**، ثم حدده لتغيير إعداداته. استخدم **Nach oben / Nach unten** لتغيير الترتيب و**Entfernen** للحذف. تُطبّق الفلاتر من أعلى القائمة إلى أسفلها، وتبدأ كل عملية حساب من الصورة الأصلية دون تغييرها. يحتفظ كل عنصر بإعداداته الخاصة، وتعمل المعالجة في الخلفية.

`Main` يبدأ البرنامج، وملفات `ui` تبني الواجهة وتعرض الصور. `ImageFiles` يقرأ الملفات. `ImageFilter` يحدد واجهة الفلتر، و`FilterType` يحدد الأنواع، و`FilterStep` يحفظ الإعدادات، و`ImageFilters` ينفذ الحسابات. ملف `FilterTests` يحتوي اختبارات يمكن تشغيلها من `main` في IntelliJ.

نجح التجميع باستخدام Java 25. نجح **288 فحصًا دون نافذة** و**289 فحصًا مع اختبار فتح النافذة وإغلاقها**. تشمل الاختبارات قيم البكسلات، ترتيب الفلاتر، الحفاظ على الأصل والشفافية، الصور الصغيرة وفتح JPG وPNG ورفض الملفات غير الصالحة. لم تُختبر جميع خطوات الاستخدام بالماوس ولا اختيار الملفات بصرياً؛ جرّبها في IntelliJ حسب التعليمات أعلاه.

لم نجد التكليف الكامل للأستاذ أو برنامجه النموذجي. جميع الوظائف الإلزامية المذكورة منفذة؛ Toonify اختياري وغير منفذ، وحفظ النتائج غير مطلوب في القائمة الحالية وغير مضاف.
