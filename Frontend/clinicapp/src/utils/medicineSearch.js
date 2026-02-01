/**
 * Medicine Search Utility
 *
 * Loads alphabetical JSON chunks on demand and caches them in memory.
 * Provides fast, case-insensitive search across 253K+ Indian medicines.
 *
 * Data source: https://github.com/junioralive/Indian-Medicine-Dataset
 */

const chunkCache = new Map();

async function loadChunk(letter) {
  if (chunkCache.has(letter)) {
    return chunkCache.get(letter);
  }

  try {
    const response = await fetch(`/data/medicines/${letter}.json`);
    if (!response.ok) return [];
    const data = await response.json();
    chunkCache.set(letter, data);
    return data;
  } catch {
    return [];
  }
}

/**
 * Search for medicines by name prefix.
 * Returns up to `limit` matches sorted by relevance (exact prefix first).
 *
 * @param {string} query - The medicine name to search for
 * @param {number} limit - Max results to return (default 10)
 * @returns {Promise<Array<{name: string, composition: string}>>}
 */
export async function searchMedicine(query, limit = 10) {
  if (!query || query.length < 2) return [];

  const firstChar = query[0].toLowerCase();
  const key = /^[a-z]$/.test(firstChar) ? firstChar : 'misc';
  const medicines = await loadChunk(key);

  const lowerQuery = query.toLowerCase();

  // Exact prefix matches first, then contains matches
  const prefixMatches = [];
  const containsMatches = [];

  for (const med of medicines) {
    const lowerName = med.name.toLowerCase();
    if (lowerName.startsWith(lowerQuery)) {
      prefixMatches.push(med);
    } else if (lowerName.includes(lowerQuery)) {
      containsMatches.push(med);
    }

    if (prefixMatches.length >= limit) break;
  }

  return [...prefixMatches, ...containsMatches].slice(0, limit);
}

/**
 * Find the composition for an exact or closest medicine match.
 *
 * @param {string} name - The medicine name
 * @returns {Promise<string|null>} The composition string or null
 */
export async function getComposition(name) {
  if (!name || name.length < 2) return null;

  const results = await searchMedicine(name, 1);
  if (results.length === 0) return null;

  // Only return composition if the match is close enough
  const match = results[0];
  const lowerName = name.toLowerCase();
  const lowerMatch = match.name.toLowerCase();

  if (lowerMatch.startsWith(lowerName) || lowerName.startsWith(lowerMatch)) {
    return match.composition;
  }

  return null;
}
