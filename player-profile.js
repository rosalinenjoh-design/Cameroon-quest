(function (root, factory) {
    const players = factory();
    if (typeof module === 'object' && module.exports) module.exports = players;
    else root.QuestPlayers = players;
}(typeof globalThis === 'object' ? globalThis : this, function () {
    'use strict';

    const XP_PER_LEVEL = 250;
    const AVATARS = [
        { id: 'lion', name: 'Lion', icon: '\u{1F981}', region: 'Centre (Yaounde)' },
        { id: 'dolphin', name: 'Sawa Dolphin', icon: '\u{1F42C}', region: 'Littoral (Douala)' },
        { id: 'buffalo', name: 'Grassfields Buffalo', icon: '\u{1F403}', region: 'West (Bafoussam)' },
        { id: 'zebu', name: 'Adamawa Zebu', icon: '\u{1F402}', region: 'Adamawa (Ngaoundere)' },
        { id: 'elephant', name: 'North Elephant', icon: '\u{1F418}', region: 'North (Garoua)' },
        { id: 'camel', name: 'Far North Camel', icon: '\u{1F42A}', region: 'Far North (Maroua)' },
        { id: 'gorilla', name: 'East Gorilla', icon: '\u{1F98D}', region: 'East (Bertoua)' },
        { id: 'leopard', name: 'South Leopard', icon: '\u{1F406}', region: 'South (Ebolowa)' },
        { id: 'horse', name: 'North-West Horse', icon: '\u{1F40E}', region: 'North-West (Bamenda)' },
        { id: 'chimpanzee', name: 'South-West Chimpanzee', icon: '\u{1F412}', region: 'South-West (Buea)' }
    ];

    function levelProgress(xp) {
        if (!Number.isSafeInteger(xp) || xp < 0 || xp > Number.MAX_SAFE_INTEGER - XP_PER_LEVEL) {
            throw new RangeError('XP must be a non-negative safe integer with room for the next level.');
        }
        const level = 1 + Math.floor(xp / XP_PER_LEVEL);
        const earned = xp % XP_PER_LEVEL;
        return {
            level, earned, required: XP_PER_LEVEL, remaining: XP_PER_LEVEL - earned,
            nextLevelXp: level * XP_PER_LEVEL, percent: earned / XP_PER_LEVEL * 100
        };
    }

    return { AVATARS, XP_PER_LEVEL, levelProgress };
}));
