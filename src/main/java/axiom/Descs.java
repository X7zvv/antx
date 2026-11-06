package axiom;

import java.util.Map;

/** Short German descriptions shown in the settings panel. */
public final class Descs {
    private static final Map<String, String> D = Map.ofEntries(
        Map.entry("TriggerBot", "Schlaegt automatisch zu, wenn dein Fadenkreuz auf einen Spieler zeigt. Optional nur Crits oder nur mit Waffe."),
        Map.entry("AimAssist", "Zieht dein Visier sanft auf den naechsten Spieler im Sichtfeld."),
        Map.entry("AutoTotem", "Haelt einen Totem in der Zweithand. Mit healthBelow nur bei niedrigem Leben."),
        Map.entry("AutoCrystal", "Platziert und zerstoert End-Kristalle in der Naehe des Gegners."),
        Map.entry("CrystalPvP", "Bot: AutoCrystal + AutoTotem und haelt Kampfabstand zum naechsten Spieler."),
        Map.entry("SwordPvP", "Bot: laeuft zum naechsten Spieler und kaempft mit dem Schwert."),
        Map.entry("MacePvP", "Bot: Windladung zum Hochschiessen, dann Mace-Schlag im Fall."),
        Map.entry("Fly", "Creative-Flug. Server mit Anticheat erkennen das oft."),
        Map.entry("AutoSprint", "Rennt automatisch. omni = in alle Richtungen."),
        Map.entry("ElytraPilot", "Fliegt mit Elytra und Feuerwerk zu den Koordinaten x/z."),
        Map.entry("Freecam", "Freie Kamera. Dein Koerper bleibt stehen. WASD, Leertaste hoch, Shift runter, Strg schneller."),
        Map.entry("PlayerESP", "Leuchtende Umrisse um andere Spieler, auch durch Waende."),
        Map.entry("BlockESP", "Zeigt gewaehlte Bloecke durch Waende. Waehle mit den Schaltern oder trage eigene Woerter bei custom ein."),
        Map.entry("Fullbright", "Nachtsicht-Effekt: ueberall hell."),
        Map.entry("HUD", "Anzeige links oben (FPS, Koordinaten) und die Modulliste rechts."),
        Map.entry("BaseMap", "Chunk-Karte im HUD: rot Kisten/Spawner, gelb Tunnel/Schaechte, orange Gebautes."),
        Map.entry("AutoSell", "Oeffnet /sell, schiebt Items hinein und schliesst wieder. Wiederholt sich."),
        Map.entry("AuctionSniper", "Kauft Auktionen im Preisfenster. Preis wird aus der Item-Beschreibung gelesen."),
        Map.entry("ItemDropper", "Wirft Items weg, deren Name zu items passt (z.B. bone,blaze_rod)."),
        Map.entry("RTPBaseFinder", "Schleife: /rtp, warten, nach Basen scannen, bei Spielern warnen."),
        Map.entry("SpawnerProtect", "Wenn ein Spieler naht: Spawner abbauen und danach ausloggen."),
        Map.entry("AutoTPA", "Nimmt Teleport-Anfragen von Freunden an (oder von allen mit acceptAll)."),
        Map.entry("BaseAlert", "Warnton und Chat-Meldung, wenn ein Fremder in die Naehe kommt."),
        Map.entry("TraderFinder", "Meldet Wandering Trader und Trader-Llamas."),
        Map.entry("BaseScanner", "Analysiert Chunks beim Laden auf Basen (Kisten, Spawner, Gebautes, Tunnel). Ergebnis siehe Tab Bases."),
        Map.entry("BaseFinder", "Einfacher Scanner: meldet Chunks mit vielen Kisten oder Spawnern."),
        Map.entry("BlockFinder", "Sucht geladene Chunks nach Bloecken ab und schreibt Koordinaten in den Chat."),
        Map.entry("AutoBuilder", "Baut .litematic aus .minecraft/schematics. Starten mit /axiom build <datei>."),
        Map.entry("AreaMiner", "Baut alles in einem Quader ab. Ecken mit /axiom set AreaMiner pos1 here."),
        Map.entry("AutoMine", "Graebt einen 1x2-Tunnel geradeaus und stoppt vor Fluessigkeiten."),
        Map.entry("AutoEat", "Isst automatisch, wenn der Hunger unter den Wert faellt."),
        Map.entry("AutoTool", "Waehlt beim Abbauen das schnellste Werkzeug."),
        Map.entry("ChestStealer", "Holt Items aus offenen Kisten, Faessern und Shulkern."),
        Map.entry("AntiAFK", "Bewegt dich regelmaessig, damit du nicht gekickt wirst."),
        Map.entry("AutoLog", "Trennt die Verbindung bei wenig Leben oder wenn ein Spieler naht."),
        Map.entry("CordSnapper", "Kopiert deine Koordinaten in die Zwischenablage und schaltet sich aus."),
        Map.entry("ClientSettings", "Aussehen des Menues: Farbe (theme 0-5) und Deckkraft.")
    );
    private Descs() {}
    public static String of(String name) { return D.getOrDefault(name, ""); }
}
