/**
 * Medicine Data Processor
 *
 * Downloads the Indian Medicine Dataset from GitHub and splits it into
 * alphabetical JSON chunks for efficient client-side loading.
 *
 * Source: https://github.com/junioralive/Indian-Medicine-Dataset
 *
 * Usage: node scripts/processMedicineData.js
 */

const https = require('https');
const fs = require('fs');
const path = require('path');

const DATASET_URL = 'https://raw.githubusercontent.com/junioralive/Indian-Medicine-Dataset/main/DATA/indian_medicine_data.json';
const OUTPUT_DIR = path.join(__dirname, '..', 'public', 'data', 'medicines');

function fetchJSON(url) {
  return new Promise((resolve, reject) => {
    const request = (reqUrl) => {
      https.get(reqUrl, { headers: { 'User-Agent': 'Node.js' } }, (res) => {
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          request(res.headers.location);
          return;
        }
        if (res.statusCode !== 200) {
          reject(new Error(`HTTP ${res.statusCode}`));
          return;
        }
        let data = '';
        res.on('data', (chunk) => { data += chunk; });
        res.on('end', () => {
          try {
            resolve(JSON.parse(data));
          } catch (e) {
            reject(new Error('Failed to parse JSON: ' + e.message));
          }
        });
        res.on('error', reject);
      }).on('error', reject);
    };
    request(url);
  });
}

async function main() {
  console.log('Downloading Indian Medicine Dataset...');
  console.log('Source:', DATASET_URL);

  let rawData;
  try {
    rawData = await fetchJSON(DATASET_URL);
  } catch (err) {
    console.error('Failed to download dataset:', err.message);
    process.exit(1);
  }

  console.log(`Downloaded ${rawData.length} medicine records.`);

  // Process: extract name + composition, group by first letter
  const chunks = {};

  for (const item of rawData) {
    const name = (item.name || '').trim();
    if (!name) continue;

    const comp1 = (item['short_composition1'] || item.short_composition1 || '').trim();
    const comp2 = (item['short_composition2'] || item.short_composition2 || '').trim();
    const composition = [comp1, comp2].filter(Boolean).join(' + ');

    if (!composition) continue;

    const firstChar = name[0].toLowerCase();
    const key = /^[a-z]$/.test(firstChar) ? firstChar : 'misc';

    if (!chunks[key]) chunks[key] = [];
    chunks[key].push({ name, composition });
  }

  // Ensure output directory exists
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  }

  // Write each chunk
  let totalMedicines = 0;
  for (const [key, medicines] of Object.entries(chunks)) {
    // Sort by name for consistent ordering
    medicines.sort((a, b) => a.name.localeCompare(b.name));

    const filePath = path.join(OUTPUT_DIR, `${key}.json`);
    fs.writeFileSync(filePath, JSON.stringify(medicines));

    const sizeKB = (Buffer.byteLength(JSON.stringify(medicines)) / 1024).toFixed(1);
    console.log(`  ${key}.json: ${medicines.length} medicines (${sizeKB} KB)`);
    totalMedicines += medicines.length;
  }

  console.log(`\nDone! ${totalMedicines} medicines split into ${Object.keys(chunks).length} chunks.`);
  console.log(`Output: ${OUTPUT_DIR}`);
}

main();
