Upute za primljeni format:

Postojati će dvije datoteke za deserijalizaciju.
characters.ser će sadržavati Pair<List<TerminalCharacter>, List<NonTerminalCharacter>>
To su završni i nezavršni znakovi, tj. liste istih. Vidi klase za više (nema komentara ali
su prilično jednostavne).

tables.ser će sadržavati Pair<Table, Table> gdje je prva tablica Akcija, a druga tablica NovoStanje.
Za detalje o tim tablicama vidi knjigu. Tablice interno imaju String[][] sa indeksom, ali izvana se
mogu adresirati sa .get() funkcijom, koja prima dva stringa -> ime stanja i (ne)završni znak (ovisi
koja tablica). Akcije koje se nalaze na određenom indeksu su iste kao u knjizi:

- Pomakni(ime_stanja)
- Reduciraj(&lt;A&gt;->alpha)
- Prihvati()
- Odbaci()
- Stavi(ime_stanja)
npr., ako imamo produkciju &lt;A&gt;->aab, onda za stanje &lt;A&gt;->aa*b i završni znak b imamo definiranu
akciju Reduciraj(&lt;A&gt;->aab) koja treba skinuti desnu stranu sa stoga i staviti &lt;A&gt; na stog (to
vi implementirate)
