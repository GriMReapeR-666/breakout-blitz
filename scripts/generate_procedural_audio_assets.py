import math
import os
import struct
import sys
import wave

sys.stdout.reconfigure(encoding='utf-8')

def synthesize_wav(path: str, duration_sec: float, gen_fn, sample_rate: int = 44100):
    num_samples = int(sample_rate * duration_sec)
    with wave.open(path, 'w') as wav_file:
        # Mono, 16-bit (2 bytes per sample), 44.1kHz
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        frames = bytearray()
        for i in range(num_samples):
            t = float(i) / sample_rate
            val = gen_fn(t, duration_sec)
            # clamp between -1.0 and 1.0
            val = max(-1.0, min(1.0, val))
            sample = int(val * 32767.0)
            frames.extend(struct.pack('<h', sample))
            
        wav_file.writeframes(frames)
    print(f"Synthesized: {path} ({os.path.getsize(path)} bytes)")

def main():
    raw_dir = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\app\src\main\res\raw"
    os.makedirs(raw_dir, exist_ok=True)

    # 1. SFX Tick (Crisp micro-click, 0.015s, 2200Hz with fast exponential decay)
    def tick_fn(t, dur):
        decay = math.exp(-t * 280.0)
        return decay * math.sin(2.0 * math.pi * 2200.0 * t) * 0.4
    synthesize_wav(os.path.join(raw_dir, "sfx_tick.wav"), 0.015, tick_fn)

    # 2. SFX Cashout (Pleasant rising harmonic bell, 0.18s, 880Hz + 1760Hz)
    def cashout_fn(t, dur):
        env = math.exp(-t * 18.0)
        f = 880.0 + (t / dur) * 300.0
        return env * (0.6 * math.sin(2.0 * math.pi * f * t) + 0.4 * math.sin(2.0 * math.pi * f * 2.0 * t))
    synthesize_wav(os.path.join(raw_dir, "sfx_cashout.wav"), 0.18, cashout_fn)

    # 3. SFX Stop Loss (Low-frequency impact thud, 0.15s, 110Hz down to 55Hz)
    def stop_loss_fn(t, dur):
        env = math.exp(-t * 22.0)
        f = 110.0 - (t / dur) * 55.0
        return env * math.sin(2.0 * math.pi * f * t) * 0.7
    synthesize_wav(os.path.join(raw_dir, "sfx_stop_loss.wav"), 0.15, stop_loss_fn)

    # 4. SFX Countdown (Tactical 440Hz blip, 0.05s)
    def countdown_fn(t, dur):
        env = math.exp(-t * 35.0)
        return env * math.sin(2.0 * math.pi * 440.0 * t) * 0.5
    synthesize_wav(os.path.join(raw_dir, "sfx_countdown.wav"), 0.05, countdown_fn)

    # 5. SFX Victory (Ascending 3-tone arpeggio: C5 (523Hz), E5 (659Hz), G5 (784Hz), 0.3s)
    def victory_fn(t, dur):
        env = math.exp(-t * 8.0)
        if t < 0.1:
            f = 523.25
        elif t < 0.2:
            f = 659.25
        else:
            f = 783.99
        return env * (0.7 * math.sin(2.0 * math.pi * f * t) + 0.3 * math.sin(2.0 * math.pi * f * 2.0 * t))
    synthesize_wav(os.path.join(raw_dir, "sfx_victory.wav"), 0.30, victory_fn)

if __name__ == "__main__":
    main()
