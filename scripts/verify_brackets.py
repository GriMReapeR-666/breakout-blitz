files = [
    'app/src/main/java/com/setuprush/candy/NativeGameActivity.kt',
    'app/src/main/java/com/setuprush/candy/engine/OpponentDuelEngine.kt',
    'app/src/main/java/com/setuprush/candy/ui/ModeStrategyPickerDialog.kt',
    'app/src/main/java/com/setuprush/candy/engine/StrategyMasteryManager.kt'
]
for fp in files:
    with open(fp, 'r', encoding='utf-8') as f:
        c = f.read()
    b_ok = (c.count('{') == c.count('}')) and (c.count('(') == c.count(')')) and (c.count('[') == c.count(']'))
    print(f'{fp}: {b_ok} (curlies: {c.count("{")} vs {c.count("}")})')
