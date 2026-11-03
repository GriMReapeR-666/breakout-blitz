import fs from 'fs';

const raw = fs.readFileSync('scripts/extracted_themes_raw.js', 'utf-8');
const obj = eval('(' + raw.replace(/^I=/, '') + ')');

fs.writeFileSync('scripts/themes_catalog.json', JSON.stringify(obj, null, 2));

console.log(`Parsed ${Object.keys(obj).length} themes:`);
for (const [id, t] of Object.entries(obj)) {
    console.log(`- ${id.padEnd(10)}: ${t.name.padEnd(12)} (bg: ${t.bg}, text: ${t.text}, bull: ${t.bull}, bear: ${t.bear})`);
}
