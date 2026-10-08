const { test } = require('node:test');
const assert = require('node:assert/strict');
const Players = require('../player-profile.js');

test('Levels advance at exactly each 250-XP threshold', () => {
    for (const [xp, level, earned, remaining] of [
        [0, 1, 0, 250], [1, 1, 1, 249], [249, 1, 249, 1],
        [250, 2, 0, 250], [251, 2, 1, 249], [499, 2, 249, 1],
        [500, 3, 0, 250], [749, 3, 249, 1], [750, 4, 0, 250], [250000, 1001, 0, 250]
    ]) {
        assert.deepEqual(Players.levelProgress(xp), {
            level, earned, required: 250, remaining, nextLevelXp: level * 250, percent: earned / 250 * 100
        });
    }
});

test('Invalid XP is reported instead of displaying a false level', () => {
    for (const xp of [-1, NaN, Infinity, 12.5, '250', null, undefined, Number.MAX_SAFE_INTEGER]) {
        assert.throws(() => Players.levelProgress(xp), RangeError);
    }
});

test('Avatar IDs, icons, and regional choices are unique and shared across client/server', () => {
    assert.equal(Players.AVATARS.length, 10);
    for (const field of ['id', 'icon', 'region']) {
        assert.equal(new Set(Players.AVATARS.map(avatar => avatar[field])).size, 10);
    }
    assert.equal(Players.AVATARS.find(avatar => avatar.id === 'lion').icon, '\u{1F981}');
    assert.ok(Players.AVATARS.every(avatar => avatar.name && avatar.region && avatar.id));
});
