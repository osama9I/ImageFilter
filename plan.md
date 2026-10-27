# Image Filter – Lernplan / خطة العمل

## Ausgangslage

- Geprüft am 27.10.2026: `src/Main.java` enthält nur das IntelliJ-Startbeispiel, noch keine Bildverarbeitung.
- Einfaches IntelliJ-Java-Projekt mit `src` als Quellordner; keine Maven- oder Gradle-Konfiguration vorhanden.
- Konsole: `java` und `javac` melden Version 26.0.1.
- Festgelegt: Wir verwenden **Java 25**. Das installierte Microsoft OpenJDK 25.0.4.1 liegt unter `C:\Users\osama\.jdks\ms-25.0.4.1` und ist in IntelliJ als `ms-25` registriert. Das Projekt verwendet dieses SDK bereits mit Sprachlevel 25; das Modul übernimmt das Projekt-SDK.
- Das vorhandene Startbeispiel wurde erfolgreich mit diesem Java 25 ausgeführt. Für weitere Konsolenprüfungen verwenden wir dessen `bin\java.exe` und `bin\javac.exe` ausdrücklich, da der globale Konsolenstandard weiterhin Java 26 ist.
- Die vollständige Aufgabenstellung und das Beispielprogramm des Dozenten sind im Projektordner nicht vorhanden. Dieser Plan beruht vorläufig auf den genannten Pflichtfunktionen. Sobald die Unterlagen vorliegen, lesen wir sie zuerst und passen den Plan an; die Vorgaben des Dozenten haben Vorrang.
- Stand nach Umsetzung: Alle sechs Pflichtfilter und die Desktop-Oberfläche sind implementiert. Vorhandene Dateien wurden gezielt angepasst; keine bestehende Datei wurde gelöscht.

## So arbeiten wir

Die neue Anweisung erlaubt die vollständige Umsetzung ohne Zwischenstopps. Die Pflichtfunktionen wurden zusammenhängend implementiert und getestet. README.md erklärt Aufbau, Bedienung und Tests auf Deutsch und Arabisch. Bestehende Dateien werden ohne deine Zustimmung nicht gelöscht.

Als vorläufige technische Grundlage verwenden wir Java Swing für die Oberfläche sowie `BufferedImage` und `ImageIO` für Bilder. Diese gehören zu Java; zusätzliche Bibliotheken sind zunächst nicht nötig. Abweichende Vorgaben des Dozenten gehen vor. Kleine Klassen sollen Fenster, Bildanzeige und Filter voneinander trennen. Wir führen sie erst ein, wenn sie gebraucht werden.

## TODO – kleine Schritte

- [x] **1. Java-Konfiguration abstimmen.** Java 25 auf deinen Wunsch festgelegt. IntelliJ-Projekt-SDK `ms-25`, Sprachlevel 25 und SDK-Vererbung des Moduls geprüft. Installiertes JDK und Compiler melden 25.0.4.1; das vorhandene Startbeispiel läuft damit erfolgreich. IntelliJ-Test: `src/Main.java` öffnen und das grüne Dreieck neben `main` anklicken. Erwartet werden „Hello and welcome!“ und die Zahlen 1 bis 5.
- [x] **2. Ein leeres Fenster erstellen.** Einen verständlichen Programmeinstieg und ein Swing-Fenster mit dem Titel „Image Filter“ aufbauen. Swing-Oberfläche im dafür vorgesehenen Ereignisthread starten. Kontrolle: Das Fenster öffnet und schließt korrekt.
- [x] **3. Drei Bereiche anlegen.** Links Filterliste und Einstellungsbereich, in der Mitte „Originalbild“, rechts „Bearbeitetes Bild“. Kontrolle: Alle Bereiche sind erkennbar und beim Vergrößern des Fensters nutzbar.
- [x] **4. JPG und PNG öffnen.** Dateiauswahl und Bildanzeige ergänzen. Große Bilder für die Anzeige passend skalieren, Bilddaten aber in Originalauflösung behalten. Abbrechen und ungültige Dateien verständlich behandeln. Kontrolle: Je ein JPG und PNG öffnen; beide Bildbereiche zeigen zunächst dasselbe Bild.
- [x] **5. Gemeinsame Filterstruktur und Invert ergänzen.** Eine kleine Filterschnittstelle einführen: Ein Filter erhält ein Bild und liefert ein neues Bild zurück. Invert ersetzt jeden RGB-Farbwert durch `255 - Wert`. Das Original bleibt unverändert; Transparenz bleibt erhalten. Kontrolle: Schwarz wird weiß, Rot wird Cyan.
- [x] **6. Filterliste bedienen.** Hinzufügen, Entfernen, Nach oben und Nach unten umsetzen. Mehrere Instanzen desselben Filters zulassen; jede behält eigene Einstellungen. Filter nacheinander auf das Ergebnis des Vorgängers anwenden, immer ausgehend vom Original. Kontrolle: Zwei Invert-Filter ergeben das Original; eine leere Liste zeigt das Original. Ungültige Aktionen deaktivieren.
- [x] **7. Brightness / Contrast ergänzen.** Regler für Helligkeit und Kontrast einführen, Farbwerte auf 0 bis 255 begrenzen. Einstellungen des ausgewählten Filters links anzeigen. Kontrolle: Neutrale Einstellungen ändern nichts; Änderungen aktualisieren das Ergebnis.
- [x] **8. Binarization / Thresholding ergänzen.** Helligkeitswert eines Pixels bestimmen und über einen einstellbaren Schwellwert Schwarz oder Weiß wählen. Kontrolle: Das Ergebnis enthält nur schwarze und weiße RGB-Werte; der Regler verändert deren Verteilung.
- [x] **9. Gaussian Blur ergänzen.** Zuerst eine kleine feste Gauß-Matrix verwenden und Nachbarpixel gewichten. Faltung und Behandlung der Bildränder einfach erklären; zusätzliche Einstellungen nur nach Bedarf oder Vorgabe. Kontrolle: Harte Übergänge werden weicher, auch kleine Bilder funktionieren.
- [x] **10. Edge Detection ergänzen.** Vorläufig Sobel zur Erkennung von Helligkeitsänderungen verwenden, sofern der Dozent keinen anderen Algorithmus vorgibt. Kontrolle: Auf einem einfachen Testbild werden die Umrisse sichtbar.
- [x] **11. Sharpening ergänzen.** Eine einfache Schärfungsmatrix verwenden und Werte begrenzen. Kontrolle: Übergänge wirken deutlicher; Bildgröße und Original bleiben erhalten.
- [x] **12. Automatische Prüfung abgeschlossen; manuelle Bedienprüfung offen (siehe unten).** Alle sechs Filter einzeln und gemeinsam prüfen. Helligkeit und Thresholding in vertauschter Reihenfolge testen: Die Reihenfolge soll das Ergebnis beeinflussen. Außerdem Auswahlwechsel, gespeicherte Filtereinstellungen, Entfernen des letzten Filters, PNG-Transparenz, kleine und große Bilder prüfen. Bei spürbaren Wartezeiten die Berechnung aus dem Oberflächenthread auslagern. In IntelliJ abschließend das gesamte Projekt bauen und starten.
- [ ] **13. Optional: Toonify.** Erst nach Abschluss der Pflichtfunktionen und deiner Anweisung planen. Mögliche Idee: Farben vereinfachen und Konturen betonen.

Die genauen Filterparameter und Algorithmen sind vorläufig, bis die vollständige Aufgabenstellung verfügbar ist. Eine Exportfunktion ist bisher nicht als Pflicht genannt und wird erst bei einer entsprechenden Vorgabe oder auf deinen Wunsch eingeplant.

## شرح مبسط بالعربية

المشروع يحتوي حالياً على مثال البداية من IntelliJ فقط. لم أجد نص التكليف الكامل ولا البرنامج النموذجي للأستاذ داخل مجلد المشروع. عندما يتوفران سنقرأهما أولاً ونعدّل الخطة حسب تعليماته.

بناءً على طلبك سنستخدم Java 25. تم التحقق من وجود Microsoft OpenJDK 25.0.4.1 ومن أن IntelliJ يستخدمه باسم `ms-25` مع مستوى لغة 25. تم تشغيل المثال الحالي بنجاح بهذه النسخة. النسخة الافتراضية في سطر الأوامر ما زالت 26، لذلك سنستخدم المسار المباشر لنسخة 25 عند الاختبار. اكتملت الخطوة الأولى؛ للاختبار في IntelliJ افتح `src/Main.java` واضغط المثلث الأخضر بجانب `main`.

سنستخدم مبدئياً Swing لبناء النافذة، و`BufferedImage` و`ImageIO` لقراءة الصور ومعالجتها. هذه الأدوات موجودة في Java ولا تحتاج إلى مكتبات إضافية.

الخطوات بالترتيب:

1. التحقق من إعداد Java وتشغيل المثال الحالي.
2. إنشاء نافذة بعنوان «Image Filter».
3. تقسيم النافذة: قائمة الفلاتر وإعداداتها يساراً، الصورة الأصلية في الوسط، والنتيجة يميناً.
4. فتح صور JPG وPNG وعرضها.
5. إنشاء واجهة بسيطة مشتركة للفلاتر وتنفيذ عكس الألوان.
6. إضافة الفلاتر وحذفها وتغيير ترتيبها، ثم تطبيقها حسب ترتيب القائمة مع الاحتفاظ بالصورة الأصلية.
7. إضافة السطوع والتباين وإعداداتهما.
8. إضافة التحويل إلى الأسود والأبيض حسب قيمة عتبة قابلة للتغيير.
9. إضافة التمويه الغاوسي وشرح حسابه باستخدام البكسلات المجاورة.
10. إضافة اكتشاف الحواف.
11. إضافة زيادة الحدة.
12. اختبار جميع الوظائف وترتيب الفلاتر والحالات الخاصة.
13. إضافة Toonify لاحقاً إذا رغبت، بعد إكمال المطلوب.

حسب تعليماتك الجديدة تم تنفيذ جميع الوظائف الإلزامية دون توقف بين الخطوات. نجح التجميع باستخدام Java 25 والاختبارات الآلية. الشرح وطريقة التشغيل موجودان في README.md. لم نحذف أي ملف موجود.

## Tatsächlicher Abschlussstand

- Java 25 bleibt Projektgrundlage. Swing-Oberfläche, JPG-/PNG-Laden, Filterliste und alle sechs Pflichtfilter sind fertig.
- Gaussian Blur hat einen einstellbaren Radius und wird in zwei Faltungsdurchläufen berechnet; Edge Detection verwendet Sobel. Bildberechnungen und Dateiladen laufen über SwingWorker im Hintergrund.
- Kompilierung mit `javac --release 25`: erfolgreich.
- Automatische Tests: 288 Prüfungen ohne Fenster und 289 einschließlich tatsächlichem Öffnen und Schließen des Fensters erfolgreich. Details und Wiederholungsanleitung stehen in README.md.
- Noch manuell zu prüfen: Dateidialog, vollständige Mausbedienung, Auswahlwechsel, letzte Filterentfernung, schnelle Regleränderungen, Fenstergrößen und die Darstellung eigener großer Fotos. Diese Bedienabläufe wurden nicht vollständig automatisiert getestet.
- Die vollständige Aufgabenstellung und das Beispiel des Dozenten fehlen weiterhin. Toonify bleibt optional und ist nicht implementiert.