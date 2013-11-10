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
- Reduciraj(A->alpha)
- Prihvati()
- Odbaci()
- Stavi(ime_stanja)
npr., ako imamo produkciju <A>->aab, onda za stanje <A>->aa*b i završni znak b imamo definiranu
akciju Reduciraj(<A>->aab) koja treba skinuti desnu stranu sa stoga i staviti <A> na stog (to
vi implementirate)
