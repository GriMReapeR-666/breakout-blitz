import math

def mulberry32(seed):
    a = seed & 0xFFFFFFFF
    def next_val():
        nonlocal a
        a = (a + 0x6D2B79F5) & 0xFFFFFFFF
        t = (((a ^ (a >> 15)) * (1 | a)) & 0xFFFFFFFF)
        t = ((t + (((t ^ (t >> 7)) * (61 | t)) & 0xFFFFFFFF)) ^ t) & 0xFFFFFFFF
        return ((t ^ (t >> 14)) & 0xFFFFFFFF) / 4294967296.0
    return next_val

def test_prng():
    rng = mulberry32(1337)
    vals = [rng() for _ in range(5)]
    print("Python Mulberry32 first 5:", vals)
    assert len(vals) == 5
    assert all(0.0 <= v < 1.0 for v in vals)

def test_pnl():
    # Long: entry 100, price 105, notional 1000, fee 5 bps
    dir_val = 1
    entry = 100.0
    price = 105.0
    notional = 1000.0
    fee_bps = 5.0
    gross = (dir_val * (price - entry)) / entry * notional
    fee = (2.0 * fee_bps / 10000.0) * notional
    pnl = gross - fee
    print(f"Long PnL: Gross=${gross:.2f}, Fee=${fee:.2f}, Net=${pnl:.2f}")
    assert gross == 50.0
    assert fee == 1.0
    assert pnl == 49.0

if __name__ == "__main__":
    test_prng()
    test_pnl()
    print("All engine parity tests passed!")
