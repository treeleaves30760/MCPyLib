"""
Performance Test: fill() vs edit()

Direct comparison of fill() and edit() for placing the SAME blocks.
Tests different region sizes to understand performance characteristics.
"""

from mcpylib import MCPyLib
import time
from dotenv import load_dotenv
import os

# Load environment variables
load_dotenv()

# Initialize client
mc = MCPyLib(
    ip=os.getenv("SERVER_IP"),
    port=65535,
    token=os.getenv("SERVER_TOKEN")
)

print("=" * 80)
print("Performance Test: fill() vs edit() - Placing SAME Blocks")
print("=" * 80)

# Get player position
player_name = input("\nEnter your Minecraft username: ").strip()
if not player_name:
    player_name = os.getenv("PLAYER_NAME", "Player")

try:
    pos = mc.getPos(player_name)
    base_x, base_y, base_z = pos
    print(f"✓ Found player '{player_name}' at ({base_x}, {base_y}, {base_z})")
    print(f"✓ Test structures will be built around your position\n")
except Exception as e:
    print(f"✗ Error: Could not find player '{player_name}': {e}")
    exit(1)

# Test configurations - different sizes to test scalability
test_configs = [
    {"name": "Small",       "size": (10, 10, 10),   "color": "🟦"},
    {"name": "Medium",      "size": (20, 20, 10),   "color": "🟨"},
    {"name": "Large",       "size": (30, 30, 10),   "color": "🟧"},
    {"name": "Very Large",  "size": (50, 50, 10),   "color": "🟥"},
    {"name": "Huge",        "size": (100, 100, 5),  "color": "🟪"},
]

results = []

print("=" * 80)
print("Starting Tests - Each size will test both fill() and edit()")
print("=" * 80)

offset_z = 0

for i, config in enumerate(test_configs, 1):
    size_x, size_y, size_z = config['size']
    total_blocks = size_x * size_y * size_z

    print(f"\n{config['color']} " + "=" * 76)
    print(f"{config['color']} Test {i}/{len(test_configs)}: {config['name']} Region")
    print(f"{config['color']} Size: {size_x}x{size_y}x{size_z} = {total_blocks:,} blocks")
    print(f"{config['color']} " + "=" * 76)

    test_result = {
        "name": config['name'],
        "size": config['size'],
        "total": total_blocks,
        "color": config['color']
    }

    # Position for fill test
    fill_x = base_x + 10
    fill_y = base_y
    fill_z = base_z + offset_z

    # Position for edit test (next to fill test)
    edit_x = fill_x + size_x + 10
    edit_y = base_y
    edit_z = base_z + offset_z

    # Test 1: fill()
    print(f"\n  [1/2] Testing fill()...")
    print(f"        Location: ({fill_x}, {fill_y}, {fill_z})")
    try:
        start_time = time.time()
        count = mc.fill(
            fill_x, fill_y, fill_z,
            fill_x + size_x - 1, fill_y + size_y - 1, fill_z + size_z - 1,
            "stone"
        )
        elapsed = time.time() - start_time
        blocks_per_sec = count / elapsed if elapsed > 0 else 0

        test_result["fill_time"] = elapsed
        test_result["fill_blocks"] = count
        test_result["fill_bps"] = blocks_per_sec

        print(f"        ✓ Time:  {elapsed:.4f} seconds")
        print(f"        ✓ Speed: {blocks_per_sec:,.0f} blocks/second")
        print(f"        ✓ Avg:   {elapsed/count*1000:.4f} ms/block")
    except Exception as e:
        print(f"        ✗ Error: {e}")
        test_result["fill_time"] = None

    time.sleep(0.2)  # Small delay between tests

    # Test 2: edit() with SAME blocks (uniform stone)
    print(f"\n  [2/2] Testing edit()...")
    print(f"        Location: ({edit_x}, {edit_y}, {edit_z})")
    try:
        # Create 3D array filled with "stone" (same as fill test)
        blocks = [[["stone" for _ in range(size_z)]
                   for _ in range(size_y)]
                  for _ in range(size_x)]

        start_time = time.time()
        count = mc.edit(edit_x, edit_y, edit_z, blocks)
        elapsed = time.time() - start_time
        blocks_per_sec = count / elapsed if elapsed > 0 else 0

        test_result["edit_time"] = elapsed
        test_result["edit_blocks"] = count
        test_result["edit_bps"] = blocks_per_sec

        print(f"        ✓ Time:  {elapsed:.4f} seconds")
        print(f"        ✓ Speed: {blocks_per_sec:,.0f} blocks/second")
        print(f"        ✓ Avg:   {elapsed/count*1000:.4f} ms/block")
    except Exception as e:
        print(f"        ✗ Error: {e}")
        test_result["edit_time"] = None

    # Compare results
    if test_result.get("fill_time") and test_result.get("edit_time"):
        print(f"\n  📊 Comparison:")
        ratio = test_result['edit_time'] / test_result['fill_time']
        time_diff = test_result['edit_time'] - test_result['fill_time']

        if ratio > 1:
            print(f"      • edit() is {ratio:.2f}x SLOWER than fill()")
            print(f"      • Difference: +{time_diff:.4f} seconds ({time_diff/test_result['fill_time']*100:.1f}%)")
        elif ratio < 1:
            print(f"      • edit() is {1/ratio:.2f}x FASTER than fill()")
            print(f"      • Difference: {time_diff:.4f} seconds ({time_diff/test_result['fill_time']*100:.1f}%)")
        else:
            print(f"      • Performance is EQUAL")

    results.append(test_result)

    # Update offset for next test
    offset_z += max(size_z, size_x + 10) + 20

# Print comprehensive summary
print("\n" + "=" * 80)
print("PERFORMANCE SUMMARY")
print("=" * 80)

print(f"\n{'Region':<12} {'Size':<15} {'Blocks':<10} {'fill()':<15} {'edit()':<15} {'Ratio':<12}")
print("-" * 80)

for result in results:
    size_str = f"{result['size'][0]}x{result['size'][1]}x{result['size'][2]}"
    blocks_str = f"{result['total']:,}"

    fill_time_str = f"{result.get('fill_time', 0):.4f}s" if result.get('fill_time') else "N/A"
    edit_time_str = f"{result.get('edit_time', 0):.4f}s" if result.get('edit_time') else "N/A"

    if result.get('fill_time') and result.get('edit_time'):
        ratio = result['edit_time'] / result['fill_time']
        if ratio > 1:
            ratio_str = f"{ratio:.2f}x slower"
        else:
            ratio_str = f"{1/ratio:.2f}x faster"
    else:
        ratio_str = "N/A"

    print(f"{result['color']} {result['name']:<10} {size_str:<15} {blocks_str:<10} {fill_time_str:<15} {edit_time_str:<15} {ratio_str}")

# Detailed analysis
print("\n" + "=" * 80)
print("DETAILED ANALYSIS")
print("=" * 80)

# Calculate average ratio
ratios = []
for result in results:
    if result.get('fill_time') and result.get('edit_time'):
        ratios.append(result['edit_time'] / result['fill_time'])

if ratios:
    avg_ratio = sum(ratios) / len(ratios)
    min_ratio = min(ratios)
    max_ratio = max(ratios)

    print(f"\n📊 Performance Ratio (edit_time / fill_time):")
    print(f"   • Average: {avg_ratio:.3f}x")
    print(f"   • Best:    {min_ratio:.3f}x (most similar)")
    print(f"   • Worst:   {max_ratio:.3f}x (largest difference)")

# Speed comparison
print(f"\n⚡ Speed Comparison:")
for result in results:
    if result.get('fill_bps') and result.get('edit_bps'):
        print(f"\n   {result['color']} {result['name']} ({result['total']:,} blocks):")
        print(f"      fill():  {result['fill_bps']:>10,.0f} blocks/sec")
        print(f"      edit():  {result['edit_bps']:>10,.0f} blocks/sec")
        diff = result['fill_bps'] - result['edit_bps']
        print(f"      diff:    {diff:>10,.0f} blocks/sec")

# Scalability analysis
print(f"\n📈 Scalability:")
if len(results) >= 2:
    print(f"\n   fill() performance:")
    for i, result in enumerate(results):
        if result.get('fill_time'):
            blocks_per_ms = result['total'] / (result['fill_time'] * 1000)
            print(f"      {result['color']} {result['name']:<12} {blocks_per_ms:.2f} blocks/ms")

    print(f"\n   edit() performance:")
    for i, result in enumerate(results):
        if result.get('edit_time'):
            blocks_per_ms = result['total'] / (result['edit_time'] * 1000)
            print(f"      {result['color']} {result['name']:<12} {blocks_per_ms:.2f} blocks/ms")

# Recommendations
print("\n" + "=" * 80)
print("RECOMMENDATIONS")
print("=" * 80)

print("\n💡 Key Insights:")

if ratios:
    if avg_ratio < 1.2:
        print("   ✓ edit() and fill() have SIMILAR performance for uniform blocks")
        print("   ✓ You can use either method without significant performance penalty")
    elif avg_ratio < 2:
        print("   ⚠️  fill() is moderately faster for uniform blocks")
        print("   • Use fill() for large uniform regions")
        print("   • Use edit() when you need flexibility (mixed blocks)")
    else:
        print("   ⚠️  fill() is significantly faster for uniform blocks")
        print("   • Always prefer fill() for large uniform regions")
        print("   • Reserve edit() for cases requiring different block types")

print("\n📋 Best Practices:")
print("   • For SAME block type:      Use fill() - optimized for this case")
print("   • For DIFFERENT block types: Use edit() - only option available")
print("   • For COMPLEX structures:    Use edit() - supports state & NBT")
print("   • For SINGLE blocks:         Use setblock()")

print("\n🎯 When to use what:")
print("   fill():     ████████████████ (uniform blocks)")
print("   edit():     ████████████░░░░ (uniform blocks)")
print("   edit():     ████████████████ (mixed blocks) - no alternative")
print("   setblock(): ████░░░░░░░░░░░░ (single blocks only)")

print("\n" + "=" * 80)
print("Test Complete! Check the structures around your position.")
print("=" * 80)

# Save results to file
try:
    with open('performance_results.txt', 'w') as f:
        f.write("MCPyLib Performance Test Results\n")
        f.write("=" * 80 + "\n\n")
        f.write(f"Player: {player_name}\n")
        f.write(f"Position: ({base_x}, {base_y}, {base_z})\n\n")

        for result in results:
            f.write(f"\n{result['name']} ({result['size'][0]}x{result['size'][1]}x{result['size'][2]}):\n")
            if result.get('fill_time'):
                f.write(f"  fill():  {result['fill_time']:.4f}s  ({result['fill_bps']:,.0f} blocks/sec)\n")
            if result.get('edit_time'):
                f.write(f"  edit():  {result['edit_time']:.4f}s  ({result['edit_bps']:,.0f} blocks/sec)\n")
            if result.get('fill_time') and result.get('edit_time'):
                ratio = result['edit_time'] / result['fill_time']
                f.write(f"  ratio:   {ratio:.3f}x\n")

    print(f"\n💾 Results saved to: performance_results.txt")
except Exception as e:
    print(f"\n⚠️  Could not save results: {e}")
