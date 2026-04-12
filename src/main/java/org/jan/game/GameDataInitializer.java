package org.jan.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the {@code games} table on startup with default game definitions.
 * Each game is inserted only if its key does not yet exist, so re-runs are
 * idempotent and manually edited rows are never overwritten.
 */
@Component
public class GameDataInitializer implements ApplicationRunner {

    @Autowired
    private GameRepository gameRepository;

    @Override
    public void run(ApplicationArguments args) {
        seed("TIC_TAC_TOE",
                "Tic-Tac-Toe",
                "❌⭕",
                "Classic 3-in-a-row strategy",
                "Tic-Tac-Toe (also known as Noughts and Crosses) is a two-player game played on a 3×3 grid. " +
                "One player is X, the other is O. Players take turns marking squares. " +
                "The first to get three of their marks in a row — horizontally, vertically, or diagonally — wins.",
                "Place three of your marks in a horizontal, vertical, or diagonal line before your opponent does. " +
                "If all nine squares are filled without a winner, the game ends in a draw.",
                List.of(
                        "Draw a 3×3 grid on paper or any flat surface.",
                        "Decide who plays ❌ and who plays ⭕ (e.g. flip a coin).",
                        "Players alternate turns, placing their mark in any empty square.",
                        "A player cannot place a mark on an already occupied square.",
                        "The game ends when one player gets three in a row or all 9 squares are filled."
                ),
                List.of(
                        "Taking the center square first is statistically the strongest opening.",
                        "If your opponent takes a corner, take the opposite corner or the center.",
                        "Always block your opponent when they have two in a row."
                )
        );

        seed("ROCK_PAPER_SCISSORS",
                "Rock Paper Scissors",
                "✊✋✌️",
                "Best of 3 — quick & fair",
                "Rock Paper Scissors (RPS) is a hand game usually played as best-of-3. " +
                "Both players simultaneously reveal one of three hand shapes. " +
                "Scissors cuts Paper, Paper covers Rock, Rock crushes Scissors.",
                "Win 2 out of 3 rounds (best of 3). " +
                "You can also agree on best of 5 for a longer match — decide before you start.",
                List.of(
                        "Players stand facing each other.",
                        "Count together: \"Rock… Paper… Scissors… Shoot!\" then reveal your hand simultaneously.",
                        "Rock (✊) beats Scissors. Scissors (✌️) beats Paper. Paper (✋) beats Rock.",
                        "A tie (both show the same shape) means that round is replayed.",
                        "Play best of 3 — the first to win 2 rounds wins the match."
                ),
                List.of(
                        "Beginners tend to throw Rock first — consider opening with Paper.",
                        "After a tie, people often switch to the shape that would have beaten their last throw.",
                        "Stay unpredictable — avoid repeating the same shape more than twice in a row."
                )
        );

        seed("TABLE_FOOTBALL",
                "Table Football",
                "⚽🏓",
                "Fast-paced foosball — first to 10",
                "Table football (foosball) is played on a table fitted with rows of rotating rods that have " +
                "miniature player figures attached. Two players (or two teams of two) control the rods on their " +
                "side and try to score goals by maneuvering the ball into the opponent's goal.",
                "Score 10 goals before your opponent does. In team play (2v2) each player controls " +
                "the rods on their half of the table.",
                List.of(
                        "Each player controls two or three rods with miniature players on their side of the table.",
                        "Serve the ball through the hole in the side of the table at the start and after each goal.",
                        "Spin shots (rotating a rod more than 360° without hitting the ball) are not allowed.",
                        "The ball must be touched by a figure before scoring — no direct long-shots from the goalie rod.",
                        "If the ball leaves the table, the last team to touch it loses possession and the opponent serves.",
                        "First to 10 goals wins. Agree beforehand if you want to play sets instead."
                ),
                List.of(
                        "Keep your wrists relaxed — tight grips slow your reaction time.",
                        "Practice the pull-shot: quickly slide a rod sideways and flick it forward.",
                        "Pin the ball with the toe of a figure to set up precision shots instead of random flicks.",
                        "Watch your opponent's goalie position and aim for the corners."
                )
        );

        seed("DARTS",
                "Šipky",
                "🎯",
                "501 dolů — kdo dřív dojde na nulu, vyhrává",
                "Šipky (501) jsou hospodská klasika pro dva hráče nebo dva týmy. Každý začíná na 501 bodech " +
                "a odečítá body za hody. Cílem je snížit skóre přesně na nulu — přičemž poslední hod musí " +
                "přistát na dvojitém poli nebo středu (bullseye).",
                "Snižuj své skóre ze 501 na přesně 0. Poslední hod musí být dvojitý (vnější kruh) nebo bullseye. " +
                "Pokud by výsledek klesl pod nulu nebo přesně na 1, kolo se nepočítá a skóre zůstává.",
                List.of(
                        "Každý hráč začíná na 501 bodech.",
                        "Na tahu jsou 3 šipky — odečítá se součet jejich bodů.",
                        "Pořadí prvního hodu se určuje hodem na střed — kdo je blíž, začíná.",
                        "Hra končí hodem na dvojité pole nebo bullseye, který sníží skóre přesně na 0.",
                        "Přehod (bust): pokud by skóre kleslo pod 0 nebo na 1, hod se nepočítá a skóre se vrátí na hodnotu před kolem."
                ),
                List.of(
                        "Zaměř se na trojité 20 — nejvýnosnější pole na terči.",
                        "Při finišování mysli dopředu: dvojka 16 vede na dvojku 8, pak na dvojku 4 a 2.",
                        "Drž loket pevně a pohybuj jen předloktím — konzistence je důležitější než síla."
                )
        );

        seed("PRSIT",
                "Prší",
                "🃏",
                "Karetní klasika — zbav se všech karet",
                "Prší je česká karetní hra pro 2–4 hráče s klasickým německým balíčkem 32 karet. " +
                "Hráči se snaží zbavit všech svých karet tím, že přikládají karty stejné barvy nebo hodnoty. " +
                "Hra obsahuje speciální karty: eso bere 4, sedmička bere 2, svršek (kluk) mění barvu a eso nebo " +
                "černý král zastavuje hru.",
                "První hráč, který se zbaví všech svých karet, vyhrává kolo. Hraje se na dohodnutý počet kol " +
                "nebo bodů — hráč s nejvyšším skóre na konci prohrává.",
                List.of(
                        "Každý hráč dostane 4 karty, jedna je odkrytá jako startovní a zbytek tvoří dobírací balíček.",
                        "Hráč přikládá kartu stejné barvy nebo hodnoty jako vrchní karta odkladiště.",
                        "Nelze-li přiložit, dobírá se ze zbytku balíčku.",
                        "Svršek (kluk): hráč smí přiložit na jakoukoli kartu a zvolí novou barvu.",
                        "Sedmička: příští hráč musí dobrat 2 karty (nebo přiložit další sedmičku — pak bere 4 atd.).",
                        "Eso: příští hráč přeskočí tah (nebo přiloží vlastní eso).",
                        "Kdo se zbaví posledních karet, vyhrává. Zbývající hráči sčítají body v ruce."
                ),
                List.of(
                        "Drž si svršky (kluky) na konec — jsou nejmocnější záchrana.",
                        "Sleduj, jaké barvy soupeř nemá — blokuj ho změnou barvy.",
                        "Zkus se zbavit vysokých karet (eso, král) brzy, aby ti nezbyly v ruce."
                )
        );

        seed("TABLE_TENNIS",
                "Stolní tenis",
                "🏓",
                "Ping-pong — first to 11 points",
                "Stolní tenis (ping-pong) je rychlá dvorceová hra pro dva hráče. " +
                "Hráči odpalují lehký míček přes síť na stole střídavě — cílem je přimět soupeře, " +
                "aby míček nevracel nebo ho vrátil mimo stůl.",
                "Vyhraj set ziskem 11 bodů s minimálně 2bodovým náskokem. Zápas se hraje na 3 nebo 5 setů — " +
                "kdo vyhraje více setů, vítězí v zápase.",
                List.of(
                        "Na začátku každého setu se losuje podání (los nebo míček za záda).",
                        "Každý hráč podává 2× po sobě, pak se podání střídá.",
                        "Při podání musí míček nejprve odskočit na straně podávajícího, pak přes síť na stranu příjemce.",
                        "Míček musí při každém odehrání přeletět přes síť a dopadnout na stůl soupeře.",
                        "Bod ztrácí hráč, který netrefí stůl, sítí, dvakrát odskočí míček u něj nebo se dotkne stolu volnou rukou.",
                        "Při stavu 10:10 se hraje na dva body rozdílu (deuce)."
                ),
                List.of(
                        "Kombinuj topspin a slice — soupeř hůř odhadne rotaci.",
                        "Stůj blízko stolu pro rychlé hry; ustup při obraně.",
                        "Sleduj, co soupeři nejde — útočí opakovaně do jeho slabší strany."
                )
        );

        seed("POOL",
                "Kulečník (8-ball)",
                "🎱",
                "Potop všechny své koule a pak osmičku",
                "8-ball pool je nejrozšířenější kulečníková hra pro dva hráče. Hraje se s 15 plnými koulemi " +
                "(1–7 plné, 9–15 pruhované) a bílou koulí. Každý hráč má přidělenu skupinu (plné nebo pruhované) " +
                "a snaží se jako první zahnat všechny svoje koule a nakonec osmičku.",
                "Zahnat všechny koule své skupiny (plné nebo pruhované) do kapsy a pak přesně zasáhnout osmičku. " +
                "Kdo zahání osmičku předčasně nebo ji pošle do špatné kapsy, prohrává.",
                List.of(
                        "Sestavení: trojúhelník, osmička uprostřed, vrchol na tečce, střídají se plné a pruhované.",
                        "Rozehrání: rozbíjející hráč musí rozehnat alespoň 4 koule ke stěně nebo zahnát kouli.",
                        "Skupina se přidělí prvním zahnaným míčem po rozehrání.",
                        "Hráč hraje, dokud zahání koule své skupiny. Chybí-li, hraje soupeř.",
                        "Osmička se hraje jako poslední — hráč musí před úderem nahlásit kapsu.",
                        "Bílá v kapse nebo osmička před čase = prohra."
                ),
                List.of(
                        "Mysli dva tahy dopředu — plánuj, kde skončí bílá po ráně.",
                        "Jemné údery s vrchním spinnem posouvají bílou kupředu; spodní spin ji stahují zpět.",
                        "Nespěchej na osmičku — nejdřív ulož všechny svoje koule do výhodných pozic."
                )
        );

        seed("JENGA",
                "Jenga",
                "🏗️",
                "Vytáhni dílec, aniž by věž spadla",
                "Jenga je hra obratnosti pro 2 a více hráčů. Věž je postavena z 54 dřevěných dílců " +
                "ve vrstvách po třech, střídavě otočených o 90°. Hráči střídavě vytahují jeden dílec " +
                "a pokládají ho nahoru — věž se postupně destabilizuje.",
                "Nevyhrává ten, kdo věž postaví nejvýš, ale ten, kdo neshodí věž jako poslední. " +
                "Hráč, který způsobí pád věže, prohrává.",
                List.of(
                        "Věž se staví ze 18 vrstev po 3 dílcích — každá vrstva je kolmo na předchozí.",
                        "Na tahu smíš vytáhnout pouze jeden dílec.",
                        "Dílec lze vytahovat jedinou rukou (nebo prstem druhé ruky jen k přidržení věže).",
                        "Nesmíš vytahovat dílce ze tří nejvyšších vrstev.",
                        "Vytažený dílec musíš položit na vrchol věže ještě před tím, než na tah přijde soupeř.",
                        "Hráč, který způsobí pád věže nebo ji shodí při pokládání dílce, prohrává."
                ),
                List.of(
                        "Poklepej na dílec prstem — zjistíš, zda je volný, nebo drží váhu.",
                        "Prostřední dílce bývají nejvolnější; krajní dílce nesou více váhy věže.",
                        "Pokládej dílce rovnoměrně — přetěžování jedné strany věže zrychlí její pád."
                )
        );

        seed("CONNECT_FOUR",
                "Čtyři v řadě",
                "🔴🟡",
                "Vyložte čtyři žetony za sebou — vodorovně, svisle nebo diagonálně",
                "Čtyři v řadě je strateg ická hra pro dva hráče na svislé mřížce 6×7 polí. " +
                "Hráči střídavě házejí barevné žetony do sloupců — žeton vždy padne na nejnižší volné místo. " +
                "Cílem je jako první sestavit čtveřici vlastních žetonů v řadě.",
                "Sestav jako první čtyři žetony za sebou — vodorovně, svisle nebo diagonálně. " +
                "Zaplní-li se celá mřížka bez vítěze, hra končí remízou.",
                List.of(
                        "Hrací pole má 6 řad a 7 sloupců; žetony padají dolů vlivem gravitace.",
                        "Hráči se střídají — každý za tah vhodí jeden žeton do libovolného neplného sloupce.",
                        "Vítěz je ten, kdo jako první spojí čtyři vlastní žetony v řadě (vodorovně, svisle nebo diagonálně).",
                        "Pokud je pole plné a nikdo nevyhrál, hra končí remízou."
                ),
                List.of(
                        "Obsaď střední sloupec — je součástí nejvíce výherních kombinací.",
                        "Vytvárej dvojí hrozbu (two-way threat): soupeř nemůže blokovat obě najednou.",
                        "Dávej pozor na diagonály — jsou nejhůře čitelné."
                )
        );

        seed("ARM_WRESTLING",
                "Přetlačování",
                "💪",
                "Přitlač soupeřovu ruku ke stolu",
                "Přetlačování (arm wrestling) je silový souboj dvou hráčů. Hráči stojí nebo sedí naproti sobě, " +
                "sevřou si pravé (nebo levé) ruce a na povel se snaží přitlačit soupeřovu ruku ke stolu.",
                "Přitlač soupeřovu ruku tak, aby se dotkla podložky (nebo pásky/linky označující konec). " +
                "Hraje se na 2 vítězná kola z 3 (best of 3).",
                List.of(
                        "Hráči postaví lokty na vymezené místo na stole — lokty musí zůstat na místě po celý zápas.",
                        "Stisku rukou rozhodčí nebo oba hráči nastavují do neutrální polohy.",
                        "Na povel 'Připravit — teď!' začíná souboj.",
                        "Přerušení: pokud hráč zvedne loket nebo vstane ze sedadla, kolo se opakuje.",
                        "Faul: použití ramenního švu (shoulder pin) nebo kroucení zápěstím za záda soupeře je zakázáno.",
                        "Vítěz kola je ten, kdo přitlačí soupeřovu ruku ke stolu. Hraje se best of 3."
                ),
                List.of(
                        "Nastav zápěstí mírně nahoru (tzv. top roll) — soupeři se ruka hůř drží.",
                        "Explozivní start je klíčový — kdo ovládne první 2 sekundy, má výhodu.",
                        "Využij celou váhu těla — opři se do ramene, ne jen do paže."
                )
        );

        seed("MEXICO_DICE",
                "Mexiko",
                "🎲",
                "Kostky — kdo hodí nejhůř, dostane žeton",
                "Mexiko je hospodská kostkovaná hra pro 2–8 hráčů. Každý hráč hodí dvěma kostkami a výsledek " +
                "se čte jako dvouciferné číslo (vyšší cifra vpředu). Speciální kombinace 2–1 je Mexiko — " +
                "nejvyšší možný výsledek. Hráč s nejnižším hodem dostane žeton (nebo 'život'); kdo nasbírá " +
                "tři žetony, vypadá ze hry.",
                "Vyhni se nejnižšímu hodu v každém kole. Kdo nasbírá tři žetony (životy), vypadá. " +
                "Poslední hráč ve hře vyhrává.",
                List.of(
                        "Každý hráč má na začátku 3 životy (žetony nebo kamínky).",
                        "Hráči házejí dvěma kostkami za clonu — výsledek vidí jen oni, soupeři ne.",
                        "Výsledek se čte jako číslo: 6+1 = 61, 3+2 = 32 atd. Vyšší číslo je lepší.",
                        "Speciální kombinace: 2–1 (Mexiko) = nejvyšší; dvojice (pár) = 11 × hodnota kostky (11, 22, … 66).",
                        "Hráč může hodit 1×, 2× nebo 3×; každé přehozené hody jsou konečné.",
                        "Na konci kola hráč s nejnižším hodem ztrácí jeden život.",
                        "Kdo přijde o všechny životy, vypadá. Poslední hráč vyhrává."
                ),
                List.of(
                        "Bluffuj — ostatní nevidí tvůj hod; přiznej méně, než máš, aby přehazovali.",
                        "Pokud máš dobrý hod hned napoprvé, nepřehazuj — zbytečně neriskuj.",
                        "Sleduj, kolik životů soupeři zbývá — útočíš jinak na hráče s 1 životem než na toho s 3."
                )
        );

        seed("DOMINO",
                "Domino",
                "🁣",
                "Zbav se všech dlaždic jako první",
                "Domino je hra pro 2–4 hráče s 28 dlaždicemi (dvojitá šestka: 0–0 až 6–6). " +
                "Hráči střídavě přikládají dlaždice tak, aby se čísla na krajích řetězu shodovala. " +
                "Kdo se jako první zbaví všech svých dlaždic, vyhrává kolo.",
                "Jako první se zbav všech svých dlaždic. Nelze-li hrát a ani táhnout ze zbytku, kolo vyhrává " +
                "hráč s nejnižším součtem bodů v ruce.",
                List.of(
                        "Každý hráč si losuje 7 dlaždic (při 2 hráčích), zbytek tvoří dobírací hromadu.",
                        "Kdo má dvojitou šestku (nebo nejvyšší dvojku), začíná a položí ji jako první.",
                        "Hráči střídavě přikládají dlaždici k jednomu z otevřených konců řetězu — čísla se musí shodovat.",
                        "Nelze-li přiložit, táhne se ze zásoby (pokud není prázdná); jinak se tah přeskočí.",
                        "Kdo se jako první zbaví všech dlaždic, vyhrává kolo a sbírá body (součet karet zbývajících soupeřům).",
                        "Hraje se do 100 bodů (nebo dohodnutého počtu)."
                ),
                List.of(
                        "Drž si dlaždice s obou konců — zachováš si více možností přiložení.",
                        "Všímej si, jaká čísla soupeři nehrají — pravděpodobně je nemají.",
                        "Hra blokováním (uzavření obou konců) může soupeře donutit táhnout."
                )
        );

        seed("MAU_MAU",
                "Mau Mau",
                "🃏",
                "Karetní hra — kdo první dohraje karty, vyhrává",
                "Mau Mau je oblíbená německá karetní hra podobná českému Prší. Hraje se s 32kartovým " +
                "(nebo 52kartovým) balíčkem pro 2–6 hráčů. Hráči přikládají karty podle barvy nebo hodnoty " +
                "na odkladiště. Speciální karty přidávají akce: sedmička = ber 2, kluk = přeji si barvu, " +
                "eso = stop, dáma = přeskočení.",
                "Jako první se zbav všech svých karet. Poslední kartu musíš ohlásit slovy 'Mau!' — " +
                "zapomeneš-li, bereš trestnou kartu.",
                List.of(
                        "Každý dostane 5 karet; jedna je odkrytá jako startovní, zbytek tvoří balíček.",
                        "Na tahu přiložíš kartu stejné barvy nebo hodnoty jako vrchní karta odkladiště.",
                        "Nemáš-li co přiložit, bereš kartu z balíčku (a hraješ, pokud ji lze přiložit).",
                        "Kluk (J): přiložíš ho na cokoli a přeješ si novou barvu.",
                        "Sedmička: příští hráč bere 2 karty (nebo přiloží další sedmičku — pak bere 4 atd.).",
                        "Eso: příští hráč přeskočí tah.",
                        "Předposlední kartu oznam 'Mau!', poslední 'Mau-Mau!'. Zapomeneš-li, bereš 2 trestné karty."
                ),
                List.of(
                        "Hlídej si kluky — jsou nejmocnější záchrana, use them late.",
                        "Sleduj počet karet soupeřů a blokuj hodem esa nebo sedmičky.",
                        "Změnou barvy klukem přepni na barvu, kterou soupeři nemají."
                )
        );

        seed("BEER_PONG",
                "Beer Pong",
                "🏓🍺",
                "Trefuj soupeřovy pohárky — kdo vyčistí stůl, vyhrává",
                "Beer Pong je párty hra pro dva hráče (nebo dva týmy 2×2). Na každé straně stolu stojí " +
                "10 pohárků sestavených do trojúhelníku. Hráči střídavě vrhají míček a snaží se trefit " +
                "do soupeřových pohárků. Hra nevyžaduje alkohol — pohárky lze naplnit vodou.",
                "Odstranit všechny pohárky soupeře dříve, než soupeř odstraní tvoje. " +
                "Kdo jako první zůstane bez pohárků, prohrává.",
                List.of(
                        "Na každé straně stolu je 10 pohárků v trojúhelníku (4–3–2–1), ve vzdálenosti cca 60 cm od kraje.",
                        "Hráči střídavě hází míček — smí odskočit od stolu nebo letět vzdušnou čarou.",
                        "Trefený pohár se odstraní (a obsah se vypije nebo vylijí, dle dohody).",
                        "Každý tým může jednou za hru požádat o přerovnání pohárků (re-rack).",
                        "Trefí-li oba hráči jednoho týmu v jednom kole, dostávají míček zpět (fire/balls back).",
                        "Tým, který jako první odstraní všechny soupeřovy pohárky, vyhrává."
                ),
                List.of(
                        "Míř na přední pohárky — při odrazu máš šanci trefit i zadní.",
                        "Konstantní technika hodu je důležitější než síla — trénuj oblouk.",
                        "Požádej o re-rack, když zbývají 6, 4, 3 nebo 2 pohárky — výhodná sestavení ti zlepší šanci."
                )
        );

        seed("BLITZ_CHESS",
                "Blitz šachy",
                "⚡♟️",
                "Šachy na čas — každý hráč má 5 minut",
                "Blitz šachy jsou rychlá varianta klasických šachů, kde každý hráč má na celou partii " +
                "jen 5 minut (nebo 3 min + 2 s přírůstek). Pravidla jsou totožná s klasickými šachy, " +
                "ale časový tlak mění hru zásadně — rozhodnutí musí být rychlá.",
                "Dej soupeřovu králi mat, nebo mu doběhni čas. Soupeř vyhrává, pokud ti doběhne čas " +
                "a na jeho straně je dostatek materiálu k matování.",
                List.of(
                        "Každý hráč má na celou partii 5 minut na hodinách (nebo 3 min + 2 s přírůstek za tah).",
                        "Po každém tahu hráč stiskne tlačítko hodin — čas přestane běžet jemu a začne soupeři.",
                        "Pravidla pohybů figur jsou stejná jako v klasických šachu.",
                        "Přepadení času (flag falls) = prohra, pokud má soupeř dostatek materiálu k matu.",
                        "Nelegální tah: soupeř může reklamovat výhru, pokud si všimne do svého dalšího tahu."
                ),
                List.of(
                        "Hrej zahájení zpaměti — nemáš čas na přemýšlení v prvních tazích.",
                        "Stiskni hodiny okamžitě po tahu — každá vteřina je cenná.",
                        "V časové tísni hrej jednoduché, bezpečné tahy — nepouštěj se do složitých kombinací."
                )
        );

        seed("NOHEJBAL",
                "Nohejbal",
                "⚽🦵",
                "Volejbal nohama — česká národní hra",
                "Nohejbal je česká míčová hra kombinující prvky fotbalu a volejbalu. Hraje se na kurtu " +
                "odděleném sítí, přičemž hráči smějí míč dotýkat pouze nohama, hlavou a hrudníkem — " +
                "rukama nesmějí. Na každé straně hraje 1, 2 nebo 3 hráči.",
                "Vyhraj set ziskem 11 bodů (nebo 15 při mezinárodních pravidlech) s minimálně 2bodovým " +
                "náskokem. Zápas se hraje na 3 vítězné sety.",
                List.of(
                        "Míč smíte hrát pouze nohama, kolenem, stehnem, bokem, břichem nebo hlavou — ruce jsou zakázány.",
                        "Každá strana smí před přehráním míče přes síť použít maximálně 3 dotyky.",
                        "Jeden hráč nesmí míč dotknout dvakrát za sebou.",
                        "Podání: podávající kopne míč přes síť — míč musí přistát v soupeřově poli.",
                        "Bod se hraje při každém podání (rally-point systém).",
                        "Set vyhrává ten, kdo dosáhne 11 bodů s 2bodovým náskokem."
                ),
                List.of(
                        "Nohejbal je hra systémem — domluv se se spoluhráči na signálech a taktice.",
                        "Teplý míč (overcut) — prudký kop shora způsobí strmý pád míče za sítí.",
                        "Udržuj míč nízko nad sítí — čím nižší přechod, tím hůře soupeř brání."
                )
        );

        seed("BOWLING",
                "Bowling",
                "🎳",
                "Sraz co nejvíce kuželek — kdo má nejvíc bodů, vyhrává",
                "Bowling je hra, při níž hráč kutálí kouli po dráze a snaží se srazit všech 10 kuželek. " +
                "Hra má 10 framů, v každém framu má hráč 2 pokusy (nebo 3 ve 10. framu při strike/spare).",
                "Nasbírej co nejvíce bodů za 10 framů. Maximum je 300 (dvanáct strike za sebou). " +
                "Hráč s nejvyšším celkovým skóre vyhrává.",
                List.of(
                        "Hra má 10 framů; každý hráč má v každém framu 2 hody.",
                        "Strike (všech 10 kuželek na první pokus): označuje se X, bonusem jsou body za příštích 2 hodů.",
                        "Spare (zbylé kuželky na druhý pokus): označuje se /, bonusem jsou body za příští 1 hod.",
                        "Otevřený frame (ani strike ani spare): počítají se jen sražené kuželky.",
                        "Ve 10. framu: strike nebo spare dávají právo na bonusové hody (celkem až 3 hody ve framu).",
                        "Hráč s nejvyšším součtem skóre po 10 framech vyhrává."
                ),
                List.of(
                        "Míř na špendlík (první kuželku) pod mírným úhlem — přímý hod nedává dostatečný pin-action.",
                        "Náběhová čára: vyber si bod na šipkách na dráze jako zaměřovací bod, ne kuželky.",
                        "Snažíš-li se o spare, přizpůsob nástupní pozici zbývajícím kuželkám."
                )
        );

        seed("TWENTY_ONE",
                "Dvacet jedna",
                "🃏21",
                "Karetní hra — přiblíž se k 21, aniž překročíš",
                "Dvacet jedna (Blackjack) je karetní hra pro 2–8 hráčů. Cílem je dostat součet karet " +
                "co nejblíže číslu 21, aniž ho přesáhneš. Hráč s nejvyšším součtem (≤ 21) vyhrává kolo. " +
                "Hra bez kasina — jeden hráč je v každém kole bankovníkem.",
                "Získej hodnotu karet co nejblíže 21 bez překročení. Hráč s vyšším součtem než bankovník " +
                "(a ≤ 21) vyhrává sázku. Přesná 21 kartami (blackjack) vítězí automaticky.",
                List.of(
                        "Hodnoty karet: čísla = jejich hodnota; J, Q, K = 10; Eso = 1 nebo 11 (hráč volí).",
                        "Bankovník rozdá každému hráči 2 karty; bankovník má jednu otevřenou, druhou zakrytou.",
                        "Hráči na tahu řeknou 'lístek' (ber kartu) nebo 'dost' (stop). Přes 21 = bust (prohra).",
                        "Bankovník nakonec odkryje zakrytou kartu a musí brát, dokud nemá alespoň 17.",
                        "Hráč vyhrává, pokud má vyšší součet než bankovník (nebo bankovník bust).",
                        "Blackjack (eso + desetihodnotová karta v prvních 2 kartách) je automatická výhra."
                ),
                List.of(
                        "Základní strategie: zastav při 17 nebo více; ber při 11 nebo méně.",
                        "Eso + šestka (soft 17) je výhodná ruka — můžeš bezpečně brát další kartu.",
                        "Pokud má bankovník 4–6, hráč může být agresivnější — bankovník má velkou šanci na bust."
                )
        );

        seed("KNIFFEL",
                "Kniffel (Yahtzee)",
                "🎲🎲",
                "5 kostek — plň kombinace a nasbírej co nejvíce bodů",
                "Kniffel (mezinárodně Yahtzee) je kostkovaná hra pro 2–6 hráčů. Každý hráč má v každém " +
                "kole až 3 hody pěti kostkami. Po každém kole zapíše výsledek do jedné z 13 kategorií. " +
                "Kdo po zaplnění všech políček nasbíral nejvíce bodů, vyhrává.",
                "Zaplň všech 13 kategorií na svém listu. Hráč s nejvyšším celkovým skóre vyhrává. " +
                "Kniffel (pět stejných čísel) přináší 50 bodů.",
                List.of(
                        "Každý hráč má v kole až 3 hody — po prvním hodu může libovolné kostky odložit a přehodit zbylé.",
                        "Po třetím hodu (nebo dříve, pokud hráč nechce házet) se výsledek zapíše do jedné kategorie.",
                        "Horní sekce (1–6): součet pouze dané hodnoty. Bonus 35 bodů za součet ≥ 63 v horní sekci.",
                        "Dolní sekce: trojka (3 stejné), čtveřice (4 stejné), full house (3+2, za 25 bodů), " +
                        "malá ulice (4 po sobě jdoucí, 30 b.), velká ulice (5 po sobě jdoucích, 40 b.), " +
                        "chance (součet všech kostek), Kniffel (5 stejných, 50 b.).",
                        "Každou kategorii lze použít pouze jednou; prázdná kategorie se škrtne za 0 bodů.",
                        "Hráč s nejvyšším celkovým skóre vyhrává."
                ),
                List.of(
                        "Zachovej si Kniffel a velkou ulici na pozdější kola — jsou nejtěžší ke splnění.",
                        "V horní sekci se snaž mít průměr alespoň 3× hodnotu na každém čísle.",
                        "Chance je záchranná kategorie — používej ji, když ti nevychází nic jiného."
                )
        );

        seed("GOMOKU",
                "Piškvorky (Gomoku)",
                "⚫⚪",
                "Pět v řadě — na papíře nebo desce",
                "Piškvorky (Gomoku) jsou strateg ická hra pro dva hráče. Na neohraničeném nebo 15×15 poli " +
                "hráči střídavě zakreslují své symboly (kolečka a křížky). Cílem je sestavit pět vlastních " +
                "symbolů v řadě — vodorovně, svisle nebo diagonálně.",
                "Sestav jako první pět svých symbolů v přímé řadě (vodorovně, svisle nebo diagonálně). " +
                "Na standardním poli 15×15 se hraje bez přesahu — přesně 5 v řadě (ne 6 nebo více).",
                List.of(
                        "Hráči se střídají; každý na tahu zaznamená svůj symbol na jedno volné pole.",
                        "Kdo sestaví pět symbolů za sebou (vodorovně, svisle nebo diagonálně), vyhrává.",
                        "Na standard ním poli 15×15 se hraje pravidlo 'přesně 5' — šestice nebo více se nepočítají.",
                        "Volitelné pravidlo swap2: černý hráč položí 2 černé a 1 bílý kámen; bílý hráč zvolí, zda vyměnit barvy."
                ),
                List.of(
                        "Útočíš-li ze dvou stran najednou (otevřená čtveřice), soupeř nemůže zablokovat obě.",
                        "Kontroluj střed desky — má nejvíce sousedních polí pro expanzi.",
                        "Hraj trojice a čtveřice na různých liniích současně — soupeř nestihne vše blokovat."
                )
        );

        seed("PENALTY_SHOOTOUT",
                "Penaltový rozstřel",
                "⚽🥅",
                "Každý kope 5 penalt — kdo dá více, vyhrává",
                "Penaltový rozstřel je fotbalová minihra pro dva hráče (nebo dva týmy). Každý hráč nebo tým " +
                "kopí 5 penalt střídavě. Potřebuješ brankáře — role se mohou střídat nebo si domluvit " +
                "dalšího hráče jako gólmana.",
                "Vstřel více branek z 5 penalt než soupeř. Při nerozhodném stavu po 5 kopech se pokračuje " +
                "náhlou smrtí (sudden death) — po 1 kopu na každou stranu, dokud jeden nevstřelí a druhý ne.",
                List.of(
                        "Hráči střídavě kopají penalty — první vybírá los.",
                        "Každý hráč kope 5 penalt; brankář se střídá nebo je určen předem.",
                        "Po 5 kolech hráč/tým s více vstřelenými brankami vyhrává.",
                        "Při remíze po 5 kolech: sudden death — každý kope 1 penaltu, dokud jeden uspěje a druhý ne.",
                        "Brankář musí stát na brankové čáře do okamžiku kopu."
                ),
                List.of(
                        "Vyber si roh předem a drž se ho — neváhej na poslední chvíli.",
                        "Variate výšku a sílu kopů — nepředvídatelnost mate brankáře.",
                        "Jako brankář čti nástupní polohu kopajícího — odrazí se do místa rozběhu."
                )
        );

        seed("BADMINTON",
                "Badminton",
                "🏸",
                "Rakety a košíček — first to 21 points",
                "Badminton je dvorceová hra pro 2 hráče (nebo 2 páry). Hráči odpalují košíček přes síť " +
                "raketami. Košíček nesmí dopadnout na zem na straně odpalujícího. Hra je extrémně rychlá " +
                "a náročná na pohyb.",
                "Vyhraj set ziskem 21 bodů s minimálně 2bodovým náskokem (maximum 30:29). " +
                "Zápas se hraje na 2 vítězné sety ze 3.",
                List.of(
                        "Podání: oba hráči musí stát ve svých polích; košíček se smí odehrát pouze pod výší pasu.",
                        "Bod se hraje při každém podání (rally-point systém).",
                        "Košíček nesmí dopadnout na zem na straně odpalujícího — to je bod pro soupeře.",
                        "Set vyhrává hráč s 21 body (s 2bodovým náskokem); při 29:29 rozhoduje jediný bod (30. bod).",
                        "Při výhře setu si hráči mění strany; při třetím setu se mění při 11 bodech."
                ),
                List.of(
                        "Smash (prudký úder shora) je nejúčinnější úder — ale vyžaduje dobrou pozici pod košíčkem.",
                        "Drop shot (jemný úder těsně za síť) nutí soupeře rychle dopředu.",
                        "Vracuj se vždy do středu kurtu po každém odehrání."
                )
        );

        seed("LIAR_DICE",
                "Lhářovy kostky",
                "🎲🤥",
                "Bluffuj nebo odhal lháře — kdo přijde o všechny kostky, prohrává",
                "Lhářovy kostky (Liar's Dice) jsou bluffovací hra pro 2–6 hráčů. Každý hráč hodí " +
                "5 kostkami a skryje je. Hráči střídavě přihazují (zvyšují bid) — říkají, kolik kostek " +
                "s danou hodnotou leží celkem na stole. Kdo si myslí, že hráč lže, vyzve ho. " +
                "Chybující hráč ztrácí kostku.",
                "Přichyť soupeře při lhaní nebo bluffuj tak přesvědčivě, aby ostatní nevyzývali. " +
                "Kdo přijde o všechny kostky, vypadá. Poslední hráč s kostkami vyhrává.",
                List.of(
                        "Každý hráč hodí 5 kostkami a skryje je pod pohár — nikdo jiný nevidí cizí kostky.",
                        "Hráči střídavě navyšují bid: říkají 'X kostek ukazuje hodnotu Y' (musí navýšit počet nebo hodnotu).",
                        "Hráč může kdykoliv vyzvat předchozího hráče ('lžeš!'): všichni odkryjí kostky.",
                        "Pokud bid byl pravdivý (tolik kostek nebo více), vyzývající ztrácí kostku.",
                        "Pokud bid byl nepravdivý, hráč, který ho řekl, ztrácí kostku.",
                        "Eso (1) je divoká karta — počítá se jako jakákoliv hodnota (volitelné pravidlo).",
                        "Kdo přijde o všechny kostky, vypadá. Poslední zbývající hráč vyhrává."
                ),
                List.of(
                        "Zapamatuj si své vlastní kostky a od nich odvoď pravděpodobnost celého stolu.",
                        "Bluffuj agresivně na začátku kola, kdy má soupeř nejméně informací.",
                        "Sleduj, kdy soupeř váhá při navýšení — pravděpodobně bluffuje."
                )
        );

        seed("FOOTBALL",
                "Fotbal",
                "⚽",
                "Klasický fotbal — kdo dá více gólů, vyhrává",
                "Fotbal je nejpopulárnější sport na světě. Hraje se mezi dvěma týmy (nebo hráči v malé formě " +
                "1v1, 2v2, 3v3, 5v5). Cílem je vstřelit soupeři více branek za dohodnutou dobu nebo do " +
                "dohodnutého počtu gólů.",
                "Vstřel soupeři více branek za dohodnutou dobu nebo jako první dosáhni dohodnutého počtu gólů. " +
                "Při remíze se hraje prodloužení nebo penaltový rozstřel.",
                List.of(
                        "Dohodněte se předem na formátu: čas (2× 10 nebo 15 minut), počet gólů nebo počet hráčů na stranu.",
                        "Hráči smí hrát míč libovolnou částí těla kromě rukou a paží (gólman v tyči smí rukama).",
                        "Míč je ve hře, dokud nepřekročí postranní nebo brankovou čáru, nebo rozhodčí neodpískal.",
                        "Faul: záměrné kopnutí soupeře, úmyslný hold nebo hra rukou = volný kop nebo penalta.",
                        "Ofsajd platí ve standardním fotbale; při malé kopané (futsal) se většinou nehraje.",
                        "Remíza: dle dohody prodloužení nebo penaltový rozstřel."
                ),
                List.of(
                        "Komunikace je klíčová — volej se spoluhráči a oznamuj svou pozici.",
                        "Bez míče se pohybuj do volného prostoru a nabízej se.",
                        "Při 1v1 si drž míč blíže a hraj s těžištěm níže — budeš stabilnější při souboji."
                )
        );

        seed("VOLLEYBALL",
                "Volejbal",
                "🏐",
                "Třídoteková hra přes síť — first to 25 points",
                "Volejbal je týmový sport 6 hráčů (nebo 2 hráčů v beach volejbalu). Hráči smějí míč " +
                "dotknout maximálně třikrát na své straně sítě a přehrát ho na stranu soupeře. " +
                "Cílem je přimět míč dopadnout na soupeřovu stranu nebo přimět soupeře k chybě.",
                "Vyhraj set ziskem 25 bodů s minimálně 2bodovým náskokem. Zápas se hraje na 3 vítězné sety " +
                "z 5 (rozhodující 5. set hraje se do 15 bodů).",
                List.of(
                        "Každý tým smí na své straně míč dotknout maximálně 3× před přehráním (podání se počítá).",
                        "Jeden hráč nesmí míč dotknout dvakrát za sebou (kromě bloku).",
                        "Podání: hráč stojí za zadní čárou a kopne nebo vybídne míč do soupeřovy části.",
                        "Bod: míč dopadne na soupeřovu stranu, soupeř chybuje nebo poruší pravidla.",
                        "Rotace: tým, který získá podání, rotuje o jednu pozici po směru hodinových ručiček.",
                        "Set do 25 bodů (2 body rozdíl); 5. set do 15 bodů."
                ),
                List.of(
                        "Nahrávka (set): měkký, kontrolovaný dotek nad sítí — umísti míč přesně pro smečaře.",
                        "Smeč: dostaň ruku co nejvýše nad síť, udeř prudce shora a zamíř do volného prostoru.",
                        "Příjem: drž nohy od sebe a paže pevně u sebe — hraje se pažemi, ne rukama."
                )
        );

        seed("BASKETBALL",
                "Basketbal",
                "🏀",
                "Košíkovaná — kdo dá více košů, vyhrává",
                "Basketbal je sport pro 5 hráčů na stranu (nebo 3v3 ve street basketball). Hráči driblují " +
                "míčem a snaží se ho hodit do soupeřova koše. Kdo nasbírá více bodů za dohodnutou dobu, vyhrává.",
                "Nasbírej více bodů za dohodnutou dobu (2× 10 minut nebo jiný formát). " +
                "Koš z pole = 2 body; za trojkovou čárou = 3 body; trestný hod = 1 bod.",
                List.of(
                        "Hráč pohybující se s míčem musí driblovat — jinak je to chůze (travelling = přestupek).",
                        "Koš z pole za trojkovou čárou = 3 body; ostatní = 2 body; trestný hod = 1 bod.",
                        "Faul: neoprávněný tělesný kontakt se soupeřem. Po 5 osobních chybách hráč opouští hru.",
                        "Trestné hody se udělují za faulování ve střeleckém pohybu nebo při przekročení limitu faulů.",
                        "Míč mimo hřiště: kdo se ho naposledy dotkl, ztrácí míč. Soupeř provede vhazování.",
                        "Dohodněte se předem na formátu: délka poločasů, pravidlo shot clock, trojková čára."
                ),
                List.of(
                        "V 1v1 využij crossover dribling — mění směr útoku a mate soupeře.",
                        "Střílej vždy s vypnutým zápěstím a vztyčeným ukazovákem — follow-through zajistí rotaci.",
                        "Hráj bez míče aktivně — nabízej se na průniková přihrávka (give-and-go)."
                )
        );

        seed("TENNIS",
                "Tenis",
                "🎾",
                "Klasický tenis — kdo vyhraje více setů, vítězí",
                "Tenis je dvorceová hra pro 2 hráče (nebo 2 páry). Hráči odpalují míček raketou přes síť " +
                "a snaží se ho umístit tak, aby ho soupeř nedokázal vrátit. Hraje se na antuce, trávě " +
                "nebo tvrdém povrchu.",
                "Vyhraj zápas ziskem 2 setů ze 3 (nebo 3 ze 5 v dlouhých formátech). Set se hraje na 6 her " +
                "(s 2 hrami rozdíl). Gamy postupují: 0–15–30–40–hra; při deuce (40:40) je nutný 2bodový náskok.",
                List.of(
                        "Podání: hráč stojí za základní čárou a servíruje míček diagonálně do soupeřova servisního pole. Má 2 pokusy.",
                        "Dvojchyba (dvojfaul): dvě chybné podání za sebou = bod soupeři.",
                        "Bod: míček je za čárou, v síti nebo se soupeř nedotkne po prvním odrazu.",
                        "Hra: 0–15–30–40–hra; při 40:40 (deuce) je nutný dvouúder na výhru (advantage).",
                        "Set: 6 her s 2hrami rozdílem. Při 6:6 se hraje tie-break (7 bodů s 2bodovým náskokem).",
                        "Zápas: 2 sety ze 3 (nebo 3 ze 5); rozhodující set někdy bez tie-breaku (do 2 her rozdílu)."
                ),
                List.of(
                        "Topspin forehand: kartáčuj míček zdola nahoru — míček se kroutí dolů a odskočí výše.",
                        "Serve and volley: po podání rychle postupuj k síti — zkrátí soupeřův reakční čas.",
                        "Cross-court je bezpečnější úder — delší vzdálenost, nižší síť v rozích."
                )
        );

        seed("CHESS",
                "Chess",
                "♟️",
                "The classic strategy board game",
                "Chess is a two-player strategy board game played on an 8×8 grid with 64 squares " +
                "alternating between light and dark colors. Each player starts with 16 pieces: " +
                "one king, one queen, two rooks, two bishops, two knights, and eight pawns. " +
                "The objective is to checkmate the opponent's king — put it under attack with no legal escape.",
                "Put your opponent's king in checkmate: a position where the king is in check (under attack) " +
                "and no legal move can escape it. The game can also end in a draw by stalemate, threefold " +
                "repetition, insufficient material, or mutual agreement.",
                List.of(
                        "White always moves first; players alternate turns.",
                        "King moves one square in any direction. It cannot move into check.",
                        "Queen moves any number of squares in any direction (row, column, or diagonal).",
                        "Rook moves any number of squares along a row or column.",
                        "Bishop moves any number of squares diagonally (stays on its starting color).",
                        "Knight moves in an L-shape: two squares in one direction, then one square perpendicular. It is the only piece that can jump over others.",
                        "Pawn moves one square forward (two on its first move) and captures diagonally.",
                        "Special moves: castling (king + rook), en passant (pawn capture), and pawn promotion (pawn reaching the last rank becomes any piece, usually a queen).",
                        "A player in check must escape it on their next move — they cannot ignore it."
                ),
                List.of(
                        "Control the center with pawns and pieces early in the game.",
                        "Develop your knights and bishops before moving the same piece twice in the opening.",
                        "Castle early to protect your king and connect your rooks.",
                        "Trade pieces when you are ahead in material; avoid trades when you are behind.",
                        "Always look for your opponent's threats before making your own move."
                )
        );
    }

    private void seed(String key, String name, String icon, String tagline,
                      String description, String howToWin,
                      List<String> rules, List<String> tips) {
        if (!gameRepository.existsByKey(key)) {
            gameRepository.save(new Game(key, name, icon, tagline, description, howToWin, rules, tips));
        }
    }
}
