import sys

sys.stdout.reconfigure(encoding='utf-8')
p = 'app/src/main/java/com/setuprush/candy/NativeBridge.kt'
with open(p, 'r', encoding='utf-8') as f:
    text = f.read()

target = """                vibrator.vibrate(millis)
            }
        } catch (e: Exception) {"""

replacement = """                vibrator.vibrate(millis)
            }

            // Sync with procedural synthesized audio
            when (type.lowercase()) {
                "tick" -> com.setuprush.candy.engine.SoundEffectManager.playTick()
                "cash_out" -> com.setuprush.candy.engine.SoundEffectManager.playCashout()
                "win" -> com.setuprush.candy.engine.SoundEffectManager.playVictory()
                "liquidation", "loss" -> com.setuprush.candy.engine.SoundEffectManager.playStopLoss()
            }
        } catch (e: Exception) {"""

text = text.replace(target.replace('\r\n', '\n'), replacement.replace('\r\n', '\n'))
text = text.replace(target, replacement)
with open(p, 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated NativeBridge.kt with SoundEffectManager sync')
