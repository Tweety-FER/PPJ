Upute za primljeni format:

Postojati će dvije datoteke za deserijalizaciju.
characters.ser će sadržavati Pair&lt;List&lt;TerminalCharacter&gt;, List&lt;NonTerminalCharacter&gt;&gt;
To su završni i nezavršni znakovi, tj. liste istih. Vidi klase za više (nema komentara ali
su prilično jednostavne).

tables.ser će sadržavati Pair&lt;Table, Table&gt; gdje je prva tablica Akcija, a druga tablica NovoStanje.
Za detalje o tim tablicama vidi knjigu. Tablice interno imaju String[][] sa indeksom, ali izvana se
mogu adresirati sa .get() funkcijom, koja prima dva stringa -&gt; ime stanja i (ne)završni znak (ovisi
koja tablica). Akcije koje se nalaze na određenom indeksu su iste kao u knjizi:

- Pomakni(ime_stanja)
- Reduciraj(&lt;A&gt;-&gt;alpha)
- Prihvati()
- Odbaci()
- Stavi(ime_stanja)
npr., ako imamo produkciju &lt;A&gt;-&gt;aab, onda za stanje &lt;A&gt;-&gt;aa*b i završni znak b imamo definiranu
akciju Reduciraj(&lt;A&gt;-&gt;aab) koja treba skinuti desnu stranu sa stoga i staviti &lt;A&gt; na stog (to
vi implementirate)
