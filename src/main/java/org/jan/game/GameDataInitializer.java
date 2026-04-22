package org.jan.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the {@code games} table on startup with bilingual (CS / EN) game definitions.
 * Each game is inserted only if its key does not yet exist — re-runs are idempotent.
 */
@Component
public class GameDataInitializer implements ApplicationRunner {

    @Autowired
    private GameRepository gameRepository;

    @Override
    public void run(ApplicationArguments args) {

        seed("TIC_TAC_TOE", "❌⭕",
                "Piškvorky 3×3", "Tic-Tac-Toe",
                "Klasická strategie — tři v řadě", "Classic 3-in-a-row strategy",
                "Piškvorky (Tic-Tac-Toe) jsou hra pro dva hráče na mřížce 3×3. Jeden hráč hraje ❌, druhý ⭕. Hráči střídavě označují pole. Kdo jako první získá tři své symboly v řadě — vodorovně, svisle nebo diagonálně — vyhrává.",
                "Tic-Tac-Toe is a two-player game played on a 3×3 grid. One player is ❌, the other ⭕. Players take turns marking squares. The first to get three marks in a row — horizontally, vertically, or diagonally — wins.",
                "Umísti tři své symboly v přímé řadě dřív než soupeř. Pokud se zaplní všech 9 polí bez vítěze, hra končí remízou.",
                "Place three of your marks in a horizontal, vertical, or diagonal line before your opponent does. If all nine squares are filled without a winner, the game is a draw.",
                List.of(
                        "Nakreslete mřížku 3×3 na papír nebo jiný plochý povrch.",
                        "Rozhodněte, kdo hraje ❌ a kdo ⭕ (např. hodem mince).",
                        "Hráči se střídají a umísťují svůj symbol na libovolné volné pole.",
                        "Na již obsazené pole nelze hrát.",
                        "Hra končí, když jeden hráč získá tři v řadě nebo jsou všechna pole obsazena."
                ),
                List.of(
                        "Draw a 3×3 grid on paper or any flat surface.",
                        "Decide who plays ❌ and who plays ⭕ (e.g. flip a coin).",
                        "Players alternate turns, placing their mark in any empty square.",
                        "A player cannot place a mark on an already occupied square.",
                        "The game ends when one player gets three in a row or all 9 squares are filled."
                ),
                List.of(
                        "Obsazení středového pole je statisticky nejsilnějším zahájením.",
                        "Pokud soupeř obsadí roh, obsaďte protilehlý roh nebo střed.",
                        "Vždy blokujte soupeře, když má dva symboly v řadě."
                ),
                List.of(
                        "Taking the center square first is statistically the strongest opening.",
                        "If your opponent takes a corner, take the opposite corner or the center.",
                        "Always block your opponent when they have two in a row."
                )
        );

        seed("ROCK_PAPER_SCISSORS", "✊✋✌️",
                "Kámen Nůžky Papír", "Rock Paper Scissors",
                "Na tři — rychlá a férovká hra", "Best of 3 — quick & fair",
                "Kámen Nůžky Papír (KNP) je ruková hra, která se hraje obvykle na tři vítězná kola. Oba hráči současně ukážou jeden ze tří tvarů ruky. Nůžky stříhají papír, papír přikrývá kámen, kámen drtí nůžky.",
                "Rock Paper Scissors (RPS) is a hand game usually played as best-of-3. Both players simultaneously reveal one of three hand shapes. Scissors cuts Paper, Paper covers Rock, Rock crushes Scissors.",
                "Vyhraj 2 ze 3 kol (best of 3). Lze se dohodnout na best of 5 pro delší zápas — rozhodněte předem.",
                "Win 2 out of 3 rounds (best of 3). You can also agree on best of 5 for a longer match — decide before you start.",
                List.of(
                        "Hráči stojí naproti sobě.",
                        "Odpočítávají spolu: 'Kámen… Nůžky… Papír… Teď!' a současně ukáží tvar ruky.",
                        "Kámen (✊) poráží Nůžky. Nůžky (✌️) porážejí Papír. Papír (✋) poráží Kámen.",
                        "Remíza (oba ukážou stejný tvar) znamená opakování kola.",
                        "Hraje se na 3 vítězná kola — kdo vyhraje 2 kola, vítězí v zápase."
                ),
                List.of(
                        "Players stand facing each other.",
                        "Count together: \"Rock… Paper… Scissors… Shoot!\" then reveal your hand simultaneously.",
                        "Rock (✊) beats Scissors. Scissors (✌️) beats Paper. Paper (✋) beats Rock.",
                        "A tie (both show the same shape) means that round is replayed.",
                        "Play best of 3 — the first to win 2 rounds wins the match."
                ),
                List.of(
                        "Začátečníci mají tendenci začínat kamenem — zvažte otevření papírem.",
                        "Po remíze lidé často přechází na tvar, který by porazil jejich poslední hod.",
                        "Buďte nepředvídatelní — vyhněte se opakování stejného tvaru více než dvakrát v řadě."
                ),
                List.of(
                        "Beginners tend to throw Rock first — consider opening with Paper.",
                        "After a tie, people often switch to the shape that would have beaten their last throw.",
                        "Stay unpredictable — avoid repeating the same shape more than twice in a row."
                )
        );

        seed("TABLE_FOOTBALL", "⚽🏓",
                "Stolní fotbal", "Table Football",
                "Rychlý stolní fotbal — první na 10 gólů", "Fast-paced foosball — first to 10",
                "Stolní fotbal (foosball) se hraje na stole s otočnými tyčemi, na nichž jsou připevněny figurky hráčů. Dva hráči (nebo dva týmy po dvou) ovládají tyče na své straně a snaží se dostat míček do soupeřovy branky.",
                "Table football (foosball) is played on a table with rotating rods that have miniature player figures. Two players (or teams of two) control the rods on their side and try to score goals.",
                "Vstřelte 10 gólů dříve než soupeř. Při hře 2v2 každý hráč ovládá tyče na své polovině stolu.",
                "Score 10 goals before your opponent does. In team play (2v2) each player controls the rods on their half of the table.",
                List.of(
                        "Každý hráč ovládá dvě nebo tři tyče s figurkami na své straně stolu.",
                        "Míček se podává otvorem v bočnici stolu na začátku a po každém gólu.",
                        "Rotační rána (otočení tyče o více než 360° bez kontaktu s míčkem) není povolena.",
                        "Před vstřelením gólu musí míček zasáhnout figurka — přímý dlouhý střel z brankářské tyče není povolen.",
                        "Pokud míček opustí stůl, podává soupeřův tým.",
                        "Kdo vstřelí 10 gólů jako první, vyhrává."
                ),
                List.of(
                        "Each player controls two or three rods with miniature players on their side of the table.",
                        "Serve the ball through the hole in the side of the table at the start and after each goal.",
                        "Spin shots (rotating a rod more than 360° without hitting the ball) are not allowed.",
                        "The ball must be touched by a figure before scoring — no direct long-shots from the goalie rod.",
                        "If the ball leaves the table, the opponent serves.",
                        "First to 10 goals wins."
                ),
                List.of(
                        "Uvolněná zápěstí — pevný úchop zpomaluje reakční čas.",
                        "Trénujte pull-shot: rychle posuňte tyč do strany a prudce ji otočte dopředu.",
                        "Přidržte míček špičkou figurky a nastavte si přesnou ránu místo náhodných úderů.",
                        "Sledujte pozici brankáře soupeře a míříte do rohů branky."
                ),
                List.of(
                        "Keep your wrists relaxed — tight grips slow your reaction time.",
                        "Practice the pull-shot: quickly slide a rod sideways and flick it forward.",
                        "Pin the ball with the toe of a figure to set up precision shots instead of random flicks.",
                        "Watch your opponent's goalie position and aim for the corners."
                )
        );

        seed("DARTS", "🎯",
                "Šipky", "Darts",
                "501 dolů — kdo dřív dojde na nulu, vyhrává", "501 down — first to reach zero wins",
                "Šipky (501) jsou hospodská klasika pro dva hráče nebo dva týmy. Každý začíná na 501 bodech a odečítá body za hody. Cílem je snížit skóre přesně na nulu — přičemž poslední hod musí přistát na dvojitém poli nebo středu (bullseye).",
                "Darts 501 is a classic pub game for two players or two teams. Each player starts at 501 and subtracts the score of each throw. The goal is to reduce the score to exactly zero — the final dart must land on a double or the bullseye.",
                "Snižuj své skóre ze 501 na přesně 0. Poslední hod musí být dvojitý (vnější kruh) nebo bullseye. Pokud by výsledek klesl pod nulu nebo přesně na 1, kolo se nepočítá a skóre zůstává.",
                "Reduce your score from 501 to exactly 0. The last dart must hit a double (outer ring) or bullseye. If the score would go below zero or land on 1, the round is busted and the score stays.",
                List.of(
                        "Každý hráč začíná na 501 bodech.",
                        "Na tahu jsou 3 šipky — odečítá se součet jejich bodů.",
                        "Pořadí prvního hodu se určuje hodem na střed — kdo je blíž, začíná.",
                        "Hra končí hodem na dvojité pole nebo bullseye, který sníží skóre přesně na 0.",
                        "Přehod (bust): pokud by skóre kleslo pod 0 nebo na 1, hod se nepočítá a skóre se vrátí na hodnotu před kolem."
                ),
                List.of(
                        "Each player starts at 501 points.",
                        "Each turn consists of 3 darts — the total scored is subtracted.",
                        "The starting order is decided by throwing closest to the bullseye.",
                        "The game ends with a dart on a double or bullseye that brings the score to exactly 0.",
                        "Bust: if the score would go below 0 or land on 1, the turn is void and the score reverts."
                ),
                List.of(
                        "Zaměř se na trojité 20 — nejvýnosnější pole na terči.",
                        "Při finišování mysli dopředu: dvojka 16 vede na dvojku 8, pak na dvojku 4 a 2.",
                        "Drž loket pevně a pohybuj jen předloktím — konzistence je důležitější než síla."
                ),
                List.of(
                        "Aim for the triple 20 — the highest-scoring segment on the board.",
                        "Plan your finish ahead: double 16 leaves double 8, then double 4 and double 2.",
                        "Keep your elbow still and move only your forearm — consistency beats power."
                )
        );

        seed("PRSIT", "🃏",
                "Prší", "Prší (Czech Crazy Eights)",
                "Karetní klasika — zbav se všech karet", "Card classic — get rid of all your cards",
                "Prší je česká karetní hra pro 2–4 hráče s klasickým německým balíčkem 32 karet. Hráči se snaží zbavit všech svých karet tím, že přikládají karty stejné barvy nebo hodnoty. Speciální karty přidávají akce: eso bere 4, sedmička bere 2, svršek mění barvu.",
                "Prší is a Czech card game for 2–4 players using a 32-card German deck. Players try to get rid of all their cards by playing a card of the same suit or rank. Special cards add actions: ace draws 4, seven draws 2, jack changes suit.",
                "První hráč, který se zbaví všech svých karet, vyhrává kolo. Hraje se na dohodnutý počet kol nebo bodů.",
                "The first player to get rid of all their cards wins the round. Play for an agreed number of rounds or points.",
                List.of(
                        "Každý hráč dostane 4 karty, jedna je odkrytá jako startovní a zbytek tvoří dobírací balíček.",
                        "Hráč přikládá kartu stejné barvy nebo hodnoty jako vrchní karta odkladiště.",
                        "Nelze-li přiložit, dobírá se ze zbytku balíčku.",
                        "Svršek (kluk): hráč smí přiložit na jakoukoli kartu a zvolí novou barvu.",
                        "Sedmička: příští hráč musí dobrat 2 karty (nebo přiložit další sedmičku — pak bere 4 atd.).",
                        "Eso: příští hráč přeskočí tah (nebo přiloží vlastní eso).",
                        "Kdo se zbaví posledních karet, vyhrává."
                ),
                List.of(
                        "Each player gets 4 cards; one card is turned face-up as the starter, the rest form the draw pile.",
                        "Play a card matching the suit or rank of the top discard.",
                        "If you cannot play, draw from the pile.",
                        "Jack: may be played on any card; the player then calls a new suit.",
                        "Seven: the next player must draw 2 cards (or play another seven — then 4, etc.).",
                        "Ace: the next player skips their turn (or plays their own ace).",
                        "The first player to empty their hand wins."
                ),
                List.of(
                        "Drž si svršky (kluky) na konec — jsou nejmocnější záchrana.",
                        "Sleduj, jaké barvy soupeř nemá — blokuj ho změnou barvy.",
                        "Zbav se vysokých karet (eso, král) brzy, aby ti nezbyly v ruce."
                ),
                List.of(
                        "Save your jacks for last — they are the most powerful escape card.",
                        "Watch which suits your opponent lacks — block them by changing the suit.",
                        "Get rid of high-value cards (ace, king) early so they don't stay in your hand."
                )
        );

        seed("TABLE_TENNIS", "🏓",
                "Stolní tenis", "Table Tennis",
                "Ping-pong — první na 11 bodů", "Ping-pong — first to 11 points",
                "Stolní tenis (ping-pong) je rychlá dvorceová hra pro dva hráče. Hráči odpalují lehký míček přes síť na stole střídavě — cílem je přimět soupeře, aby míček nevracel nebo ho vrátil mimo stůl.",
                "Table tennis (ping-pong) is a fast-paced racket sport for two players. Players alternate hitting a lightweight ball back and forth over a net — the goal is to make the opponent fail to return the ball or return it out of bounds.",
                "Vyhraj set ziskem 11 bodů s minimálně 2bodovým náskokem. Zápas se hraje na 3 nebo 5 setů.",
                "Win a set by reaching 11 points with at least a 2-point lead. A match is best of 3 or best of 5 sets.",
                List.of(
                        "Na začátku každého setu se losuje podání.",
                        "Každý hráč podává 2× po sobě, pak se podání střídá.",
                        "Při podání musí míček nejprve odskočit na straně podávajícího, pak přes síť na stranu příjemce.",
                        "Míček musí při každém odehrání přeletět přes síť a dopadnout na stůl soupeře.",
                        "Bod ztrácí hráč, který netrefí stůl, zasítí, nebo se dotkne stolu volnou rukou.",
                        "Při stavu 10:10 se hraje na dva body rozdílu (deuce)."
                ),
                List.of(
                        "The serve is decided by coin toss at the start of each set.",
                        "Each player serves twice in a row, then the serve alternates.",
                        "On serve, the ball must bounce on the server's side first, then cross the net.",
                        "Every shot must clear the net and land on the opponent's side of the table.",
                        "A point is lost if you miss the table, hit the net, or touch the table with your free hand.",
                        "At 10–10 the game continues until one player leads by 2 points (deuce)."
                ),
                List.of(
                        "Kombinuj topspin a slice — soupeř hůř odhadne rotaci.",
                        "Stůj blízko stolu pro rychlé hry; ustup při obraně.",
                        "Útočíš opakovaně do slabší strany soupeře."
                ),
                List.of(
                        "Mix topspin and slice — your opponent will struggle to read the spin.",
                        "Stand close to the table for attacking play; step back when defending.",
                        "Repeatedly target your opponent's weaker side."
                )
        );

        seed("POOL", "🎱",
                "Kulečník (8-ball)", "Pool (8-ball)",
                "Potop všechny své koule a pak osmičku", "Pot all your balls then the eight",
                "8-ball pool je nejrozšířenější kulečníková hra pro dva hráče. Hraje se s 15 barevnými koulemi (1–7 plné, 9–15 pruhované) a bílou koulí. Každý hráč má přidělenu skupinu a snaží se jako první zahnát všechny své koule a nakonec osmičku.",
                "8-ball pool is the most popular billiards game for two players. It is played with 15 object balls (1–7 solids, 9–15 stripes) and the cue ball. Each player is assigned a group and tries to pot all their balls first, then the eight ball.",
                "Zahnat všechny koule své skupiny a pak přesně zasáhnout osmičku. Kdo zahání osmičku předčasně nebo ji pošle do špatné kapsy, prohrává.",
                "Pot all your group's balls then the eight ball in a called pocket. Potting the eight ball early or in the wrong pocket loses the game.",
                List.of(
                        "Sestavení: trojúhelník, osmička uprostřed, vrchol na tečce, střídají se plné a pruhované.",
                        "Rozehrání: rozbíjející hráč musí rozehnat alespoň 4 koule ke stěně nebo zahnát kouli.",
                        "Skupina se přidělí prvním zahnaným míčem po rozehrání.",
                        "Hráč hraje, dokud zahání koule své skupiny. Chybí-li, hraje soupeř.",
                        "Osmička se hraje jako poslední — hráč musí před úderem nahlásit kapsu.",
                        "Bílá v kapse nebo osmička před čase = prohra."
                ),
                List.of(
                        "Rack: triangle, eight ball in the centre, apex on the spot, solids and stripes alternated.",
                        "Break: the breaking player must drive at least 4 balls to a cushion or pot a ball.",
                        "Group assignment is determined by the first ball potted after the break.",
                        "A player continues as long as they pot their group's balls; otherwise the opponent plays.",
                        "The eight ball is played last — the player must call the pocket.",
                        "Cueing the white into a pocket or potting the eight prematurely loses the game."
                ),
                List.of(
                        "Mysli dva tahy dopředu — plánuj, kde skončí bílá po ráně.",
                        "Vrchní spin posouvá bílou kupředu; spodní spin ji stahuje zpět.",
                        "Nespěchej na osmičku — nejdřív ulož všechny své koule do výhodných pozic."
                ),
                List.of(
                        "Think two shots ahead — plan where the cue ball will stop after each shot.",
                        "Top spin rolls the cue ball forward; back spin pulls it back.",
                        "Don't rush to the eight ball — get all your balls in good positions first."
                )
        );

        seed("JENGA", "🏗️",
                "Jenga", "Jenga",
                "Vytáhni dílec, aniž by věž spadla", "Pull a block without toppling the tower",
                "Jenga je hra obratnosti pro 2 a více hráčů. Věž je postavena z 54 dřevěných dílců ve vrstvách po třech, střídavě otočených o 90°. Hráči střídavě vytahují jeden dílec a pokládají ho nahoru — věž se postupně destabilizuje.",
                "Jenga is a dexterity game for 2 or more players. A tower is built from 54 wooden blocks in layers of three, alternating direction by 90°. Players take turns removing one block and placing it on top — the tower becomes increasingly unstable.",
                "Nevyhrává ten, kdo věž postaví nejvýš, ale ten, kdo neshodí věž jako poslední. Hráč, který způsobí pád věže, prohrává.",
                "The winner is not the one who builds the tallest tower, but the last player who does not knock it over. The player who causes the tower to fall loses.",
                List.of(
                        "Věž se staví ze 18 vrstev po 3 dílcích — každá vrstva je kolmo na předchozí.",
                        "Na tahu smíš vytáhnout pouze jeden dílec.",
                        "Dílec lze vytahovat jednou rukou (nebo prstem druhé ruky jen k přidržení věže).",
                        "Nesmíš vytahovat dílce ze tří nejvyšších vrstev.",
                        "Vytažený dílec musíš položit na vrchol věže ještě před tím, než na tah přijde soupeř.",
                        "Hráč, který způsobí pád věže nebo ji shodí při pokládání dílce, prohrává."
                ),
                List.of(
                        "The tower is built with 18 layers of 3 blocks — each layer perpendicular to the one below.",
                        "On your turn you may remove only one block.",
                        "Blocks may only be removed using one hand (the other may only steady the tower).",
                        "Blocks from the top three layers may not be removed.",
                        "The removed block must be placed on top of the tower before the next player's turn.",
                        "The player who causes the tower to fall — while removing or placing a block — loses."
                ),
                List.of(
                        "Poklepej na dílec prstem — zjistíš, zda je volný, nebo drží váhu.",
                        "Prostřední dílce bývají nejvolnější; krajní dílce nesou více váhy věže.",
                        "Pokládej dílce rovnoměrně — přetěžování jedné strany věže zrychlí její pád."
                ),
                List.of(
                        "Tap a block with your finger to tell whether it is free or load-bearing.",
                        "Middle blocks are usually the loosest; end blocks carry more of the tower's weight.",
                        "Place blocks evenly — overloading one side will speed up the tower's collapse."
                )
        );

        seed("CONNECT_FOUR", "🔴🟡",
                "Čtyři v řadě", "Connect Four",
                "Čtyři žetony za sebou — vodorovně, svisle nebo diagonálně", "Four in a row — horizontal, vertical, or diagonal",
                "Čtyři v řadě je strategická hra pro dva hráče na svislé mřížce 6×7 polí. Hráči střídavě hází barevné žetony do sloupců — žeton vždy padne na nejnižší volné místo. Cílem je jako první sestavit čtveřici vlastních žetonů v řadě.",
                "Connect Four is a strategy game for two players on a vertical 6×7 grid. Players take turns dropping coloured discs into columns — each disc falls to the lowest available space. The goal is to be the first to connect four of your discs in a row.",
                "Sestav jako první čtyři žetony za sebou — vodorovně, svisle nebo diagonálně. Zaplní-li se celá mřížka bez vítěze, hra končí remízou.",
                "Be the first to connect four of your discs in a row — horizontally, vertically, or diagonally. If the grid fills without a winner, the game is a draw.",
                List.of(
                        "Hrací pole má 6 řad a 7 sloupců; žetony padají dolů vlivem gravitace.",
                        "Hráči se střídají — každý za tah vhodí jeden žeton do libovolného neplného sloupce.",
                        "Vítěz je ten, kdo jako první spojí čtyři vlastní žetony v řadě.",
                        "Pokud je pole plné a nikdo nevyhrál, hra končí remízou."
                ),
                List.of(
                        "The grid has 6 rows and 7 columns; discs fall under gravity.",
                        "Players alternate — each turn drop one disc into any non-full column.",
                        "The winner is the first to connect four of their own discs in a row.",
                        "If the grid is full and nobody has won, the game is a draw."
                ),
                List.of(
                        "Obsaď střední sloupec — je součástí nejvíce výherních kombinací.",
                        "Vytvárej dvojí hrozbu (two-way threat): soupeř nemůže blokovat obě najednou.",
                        "Dávej pozor na diagonály — jsou nejhůře čitelné."
                ),
                List.of(
                        "Control the centre column — it is part of the most winning combinations.",
                        "Create a double threat: your opponent cannot block both at once.",
                        "Watch the diagonals — they are the hardest to read."
                )
        );

        seed("ARM_WRESTLING", "💪",
                "Přetlačování", "Arm Wrestling",
                "Přitlač soupeřovu ruku ke stolu", "Pin your opponent's arm to the table",
                "Přetlačování (arm wrestling) je silový souboj dvou hráčů. Hráči stojí nebo sedí naproti sobě, sevřou si ruce a na povel se snaží přitlačit soupeřovu ruku ke stolu.",
                "Arm wrestling is a strength contest between two players. Players stand or sit facing each other, grip hands, and on a signal try to pin the opponent's arm to the table.",
                "Přitlač soupeřovu ruku tak, aby se dotkla podložky. Hraje se na 2 vítězná kola z 3 (best of 3).",
                "Pin your opponent's arm to the pad. The match is best of 3 rounds.",
                List.of(
                        "Hráči postaví lokty na vymezené místo na stole — lokty musí zůstat na místě po celý zápas.",
                        "Uchopit ruce do neutrální polohy — rozhodčí nebo oba hráči nastaví stisku.",
                        "Na povel 'Připravit — teď!' začíná souboj.",
                        "Přerušení: pokud hráč zvedne loket nebo vstane, kolo se opakuje.",
                        "Faul: použití ramenního švu nebo kroucení zápěstím za záda soupeře je zakázáno.",
                        "Vítěz kola přitlačí soupeřovu ruku ke stolu. Hraje se best of 3."
                ),
                List.of(
                        "Both players place their elbows on the designated pad — elbows must stay in place throughout.",
                        "Grip hands in a neutral position — a referee or both players set the grip.",
                        "On the signal 'Ready — Go!' the match begins.",
                        "If a player lifts their elbow or stands up, the round is restarted.",
                        "Foul: using a shoulder roll or twisting the opponent's wrist behind their back is banned.",
                        "The round winner pins the opponent's arm. Match is best of 3."
                ),
                List.of(
                        "Nastav zápěstí mírně nahoru (top roll) — soupeři se ruka hůř drží.",
                        "Explozivní start je klíčový — kdo ovládne první 2 sekundy, má výhodu.",
                        "Využij celou váhu těla — opři se do ramene, ne jen do paže."
                ),
                List.of(
                        "Angle your wrist slightly upward (top roll) — your opponent will struggle to keep their grip.",
                        "An explosive start is key — whoever controls the first 2 seconds has the advantage.",
                        "Use your whole body weight — lean into your shoulder, not just your arm."
                )
        );

        seed("MEXICO_DICE", "🎲",
                "Mexiko", "Mexico (Dice)",
                "Kostky — kdo hodí nejhůř, dostane žeton", "Dice bluffing — lowest roll gets a token",
                "Mexiko je hospodská kostkovaná hra pro 2–8 hráčů. Každý hráč hodí dvěma kostkami a výsledek se čte jako dvouciferné číslo (vyšší cifra vpředu). Speciální kombinace 2–1 je Mexiko — nejvyšší možný výsledek. Hráč s nejnižším hodem dostane žeton; kdo nasbírá tři žetony, vypadá.",
                "Mexico is a pub dice game for 2–8 players. Each player rolls two dice and reads the result as a two-digit number (higher digit first). The special combo 2–1 is Mexico — the highest possible result. The player with the lowest roll gets a token; collect three tokens and you are out.",
                "Vyhni se nejnižšímu hodu v každém kole. Kdo nasbírá tři žetony, vypadá. Poslední hráč ve hře vyhrává.",
                "Avoid the lowest roll each round. Collect three tokens and you are eliminated. Last player remaining wins.",
                List.of(
                        "Každý hráč má na začátku 3 životy (žetony).",
                        "Hráči hází dvěma kostkami za clonu — výsledek vidí jen oni.",
                        "Výsledek se čte jako číslo: 6+1 = 61, 3+2 = 32 atd. Vyšší číslo je lepší.",
                        "Speciální kombinace: 2–1 (Mexiko) = nejvyšší; dvojice (pár) = 11 × hodnota kostky.",
                        "Hráč může hodit 1×, 2× nebo 3×; každé přehozené hody jsou konečné.",
                        "Na konci kola hráč s nejnižším hodem ztrácí jeden život.",
                        "Kdo přijde o všechny životy, vypadá. Poslední hráč vyhrává."
                ),
                List.of(
                        "Each player starts with 3 lives (tokens).",
                        "Players roll two dice behind a screen — only they see the result.",
                        "Read the result as a number: 6+1 = 61, 3+2 = 32 etc. Higher is better.",
                        "Special combos: 2–1 (Mexico) = highest; doubles = 11 × the die value.",
                        "A player may roll 1, 2, or 3 times; re-rolled dice are final.",
                        "At the end of each round, the player with the lowest roll loses one life.",
                        "A player who loses all lives is eliminated. Last player wins."
                ),
                List.of(
                        "Bluffuj — ostatní nevidí tvůj hod; přiznej méně, než máš.",
                        "Pokud máš dobrý hod hned napoprvé, nepřehazuj — zbytečně neriskuj.",
                        "Sleduj, kolik životů soupeři zbývá — útočíš jinak na hráče s 1 životem."
                ),
                List.of(
                        "Bluff — others cannot see your roll; claim less than you have.",
                        "If you roll well on the first throw, don't re-roll — no point risking a worse result.",
                        "Watch how many lives opponents have left — play differently against someone on their last life."
                )
        );

        seed("DOMINO", "🁣",
                "Domino", "Domino",
                "Zbav se všech dlaždic jako první", "Get rid of all your tiles first",
                "Domino je hra pro 2–4 hráče s 28 dlaždicemi (dvojitá šestka: 0–0 až 6–6). Hráči střídavě přikládají dlaždice tak, aby se čísla na krajích řetězu shodovala. Kdo se jako první zbaví všech svých dlaždic, vyhrává kolo.",
                "Domino is a game for 2–4 players using 28 tiles (double-six set: 0–0 to 6–6). Players take turns placing tiles so the numbers at the chain ends match. The first player to play all their tiles wins the round.",
                "Jako první se zbav všech svých dlaždic. Nelze-li hrát a ani táhnout ze zbytku, kolo vyhrává hráč s nejnižším součtem bodů v ruce.",
                "Be the first to empty your hand. If nobody can play or draw, the player with the lowest pip count in hand wins the round.",
                List.of(
                        "Každý hráč si losuje 7 dlaždic (při 2 hráčích), zbytek tvoří dobírací hromadu.",
                        "Kdo má dvojitou šestku (nebo nejvyšší dvojku), začíná a položí ji jako první.",
                        "Hráči střídavě přikládají dlaždici k jednomu z otevřených konců řetězu — čísla se musí shodovat.",
                        "Nelze-li přiložit, táhne se ze zásoby; jinak se tah přeskočí.",
                        "Kdo se jako první zbaví všech dlaždic, vyhrává kolo.",
                        "Hraje se do 100 bodů."
                ),
                List.of(
                        "Each player draws 7 tiles (for 2 players); the rest form a draw pile.",
                        "The player with the double-six (or highest double) starts by playing it first.",
                        "Players alternate placing a tile at either open end of the chain — numbers must match.",
                        "If you cannot play, draw from the pile; if the pile is empty, pass.",
                        "The first player to empty their hand wins the round.",
                        "Play to 100 points."
                ),
                List.of(
                        "Drž si dlaždice s obou konců — zachováš si více možností přiložení.",
                        "Všímej si, jaká čísla soupeři nehrají — pravděpodobně je nemají.",
                        "Hra blokováním (uzavření obou konců) může soupeře donutit táhnout."
                ),
                List.of(
                        "Keep tiles that cover both open ends — you will have more options.",
                        "Notice which numbers opponents never play — they probably don't have them.",
                        "Blocking play (closing both ends of the chain) can force opponents to draw."
                )
        );

        seed("MAU_MAU", "🃏",
                "Mau Mau", "Mau Mau",
                "Kdo první dohraje karty, vyhrává", "First to play all cards wins",
                "Mau Mau je oblíbená německá karetní hra podobná českému Prší. Hraje se s 32kartovým balíčkem pro 2–6 hráčů. Speciální karty přidávají akce: sedmička = ber 2, kluk = přeji si barvu, eso = stop.",
                "Mau Mau is a popular German card game similar to Crazy Eights. It is played with a 32-card deck for 2–6 players. Special cards add actions: seven = draw 2, jack = wish a suit, ace = skip.",
                "Jako první se zbav všech svých karet. Předposlední kartu ohlásí hráč slovy 'Mau!' — zapomeneš-li, bereš trestnou kartu.",
                "Be the first to play all your cards. When playing your second-to-last card, say 'Mau!' — forget and you draw a penalty card.",
                List.of(
                        "Každý dostane 5 karet; jedna je odkrytá jako startovní.",
                        "Na tahu přiložíš kartu stejné barvy nebo hodnoty jako vrchní karta odkladiště.",
                        "Nemáš-li co přiložit, bereš kartu z balíčku.",
                        "Kluk (J): přiložíš ho na cokoli a přeješ si novou barvu.",
                        "Sedmička: příští hráč bere 2 karty (nebo přiloží další sedmičku).",
                        "Eso: příští hráč přeskočí tah.",
                        "Předposlední kartu oznam 'Mau!', poslední 'Mau-Mau!'. Zapomeneš-li, bereš 2 trestné karty."
                ),
                List.of(
                        "Each player gets 5 cards; one card is turned face-up as the starter.",
                        "Play a card matching the suit or rank of the top discard.",
                        "If you cannot play, draw a card from the pile.",
                        "Jack: may be played on anything; you then wish a new suit.",
                        "Seven: the next player draws 2 cards (or plays another seven).",
                        "Ace: the next player skips their turn.",
                        "Say 'Mau!' with your second-to-last card, 'Mau-Mau!' with your last. Forget and draw 2 penalty cards."
                ),
                List.of(
                        "Hlídej si kluky — jsou nejmocnější záchrana, použij je na konci.",
                        "Sleduj počet karet soupeřů a blokuj hodem esa nebo sedmičky.",
                        "Změnou barvy klukem přepni na barvu, kterou soupeři nemají."
                ),
                List.of(
                        "Guard your jacks — they are the most powerful cards; use them late.",
                        "Watch how many cards opponents have and block them with an ace or seven.",
                        "Use a jack to switch to a suit your opponents are missing."
                )
        );

        seed("BEER_PONG", "🏓🍺",
                "Beer Pong", "Beer Pong",
                "Trefuj soupeřovy pohárky — kdo vyčistí stůl, vyhrává", "Hit opponent's cups — clear the table to win",
                "Beer Pong je párty hra pro dva hráče nebo dva týmy 2×2. Na každé straně stolu stojí 10 pohárků sestavených do trojúhelníku. Hráči střídavě vrhají míček a snaží se trefit soupeřovy pohárky. Hra nevyžaduje alkohol — pohárky lze naplnit vodou.",
                "Beer Pong is a party game for two players or two teams of 2. Ten cups are arranged in a triangle on each side of the table. Players take turns throwing a ball into the opponent's cups. No alcohol required — cups can be filled with water.",
                "Odstranit všechny pohárky soupeře dříve, než soupeř odstraní tvoje. Kdo první zůstane bez pohárků, prohrává.",
                "Remove all of your opponent's cups before they remove yours. The first side left without cups loses.",
                List.of(
                        "Na každé straně stolu je 10 pohárků v trojúhelníku (4–3–2–1).",
                        "Hráči střídavě hází míček — smí odskočit od stolu nebo letět vzdušnou čarou.",
                        "Trefený pohár se odstraní.",
                        "Každý tým může jednou za hru požádat o přerovnání pohárků (re-rack).",
                        "Trefí-li oba hráči jednoho týmu v jednom kole, dostávají míček zpět.",
                        "Tým, který jako první odstraní všechny soupeřovy pohárky, vyhrává."
                ),
                List.of(
                        "Ten cups are arranged in a 4–3–2–1 triangle on each side of the table.",
                        "Players alternate throwing — the ball may bounce off the table or fly through the air.",
                        "A hit cup is removed.",
                        "Each team may call one re-rack per game.",
                        "If both players on a team hit in the same round, they get the balls back.",
                        "The team that removes all of the opponent's cups first wins."
                ),
                List.of(
                        "Míř na přední pohárky — při odrazu máš šanci trefit i zadní.",
                        "Konstantní technika hodu je důležitější než síla — trénuj oblouk.",
                        "Požádej o re-rack při 6, 4, 3 nebo 2 pohárků — výhodná sestavení zlepší šanci."
                ),
                List.of(
                        "Aim for the front cups — a bounce can still reach the back ones.",
                        "Consistent throwing technique beats power — practice your arc.",
                        "Call re-rack at 6, 4, 3, or 2 cups remaining — favourable formations improve your odds."
                )
        );

        seed("BLITZ_CHESS", "⚡♟️",
                "Blitz šachy", "Blitz Chess",
                "Šachy na čas — každý hráč má 5 minut", "Speed chess — each player has 5 minutes",
                "Blitz šachy jsou rychlá varianta klasických šachů, kde každý hráč má na celou partii jen 5 minut. Pravidla jsou totožná s klasickými šachy, ale časový tlak mění hru zásadně.",
                "Blitz chess is a fast-paced variant of chess where each player has only 5 minutes for the entire game. Rules are identical to classical chess, but time pressure changes the game fundamentally.",
                "Dej soupeřovu králi mat, nebo mu doběhni čas. Soupeř vyhrává, pokud ti doběhne čas a má dostatek materiálu k matování.",
                "Checkmate your opponent's king or run their clock to zero. Your opponent wins if your flag falls and they have sufficient mating material.",
                List.of(
                        "Každý hráč má na celou partii 5 minut (nebo 3 min + 2 s přírůstek za tah).",
                        "Po každém tahu hráč stiskne tlačítko hodin.",
                        "Pravidla pohybů figur jsou stejná jako v klasických šachu.",
                        "Přepadení času = prohra, pokud má soupeř dostatek materiálu k matu.",
                        "Nelegální tah: soupeř může reklamovat výhru, pokud si všimne do svého dalšího tahu."
                ),
                List.of(
                        "Each player has 5 minutes for the whole game (or 3 min + 2 s increment per move).",
                        "After each move the player hits the clock button.",
                        "Piece movement rules are the same as in classical chess.",
                        "Flag fall = loss, provided the opponent has sufficient mating material.",
                        "Illegal move: the opponent may claim a win if they notice before their own next move."
                ),
                List.of(
                        "Hrej zahájení zpaměti — nemáš čas na přemýšlení v prvních tazích.",
                        "Stiskni hodiny okamžitě po tahu — každá vteřina je cenná.",
                        "V časové tísni hrej jednoduché, bezpečné tahy."
                ),
                List.of(
                        "Play the opening from memory — there is no time to think in the first moves.",
                        "Hit the clock immediately after each move — every second counts.",
                        "When short on time, play simple, safe moves rather than complex combinations."
                )
        );

        seed("TWENTY_ONE", "🃏",
                "Dvacet jedna", "Blackjack (21)",
                "Přiblíž se k 21, aniž překročíš", "Get as close to 21 as possible without going over",
                "Dvacet jedna (Blackjack) je karetní hra pro 2–8 hráčů. Cílem je dostat součet karet co nejblíže číslu 21, aniž ho přesáhneš. Jeden hráč je v každém kole bankovníkem.",
                "Blackjack (21) is a card game for 2–8 players. The goal is to get a hand value as close to 21 as possible without exceeding it. One player acts as the dealer each round.",
                "Získej hodnotu karet co nejblíže 21 bez překročení. Hráč s vyšším součtem než bankovník (a ≤ 21) vyhrává. Přesná 21 dvěma kartami (blackjack) vítězí automaticky.",
                "Get a hand total closer to 21 than the dealer without busting. A player with a higher total than the dealer (and ≤ 21) wins the bet. A natural blackjack (21 with two cards) wins automatically.",
                List.of(
                        "Hodnoty karet: čísla = jejich hodnota; J, Q, K = 10; Eso = 1 nebo 11 (hráč volí).",
                        "Bankovník rozdá každému hráči 2 karty; bankovník má jednu otevřenou, druhou zakrytou.",
                        "Hráči říkají 'lístek' (ber kartu) nebo 'dost' (stop). Přes 21 = bust.",
                        "Bankovník odkryje zakrytou kartu a musí brát, dokud nemá alespoň 17.",
                        "Hráč vyhrává, pokud má vyšší součet než bankovník (nebo bankovník bust).",
                        "Blackjack (eso + desetihodnotová karta v prvních 2 kartách) je automatická výhra."
                ),
                List.of(
                        "Card values: number cards = face value; J, Q, K = 10; Ace = 1 or 11 (player's choice).",
                        "The dealer gives each player 2 cards; the dealer has one face-up and one face-down.",
                        "Players say 'hit' (take a card) or 'stand' (stop). Exceed 21 = bust.",
                        "The dealer reveals their hidden card and must hit until reaching at least 17.",
                        "A player wins if their total exceeds the dealer's (or the dealer busts).",
                        "Blackjack (ace + ten-value card in the first 2 cards) is an automatic win."
                ),
                List.of(
                        "Základní strategie: zastav při 17 nebo více; ber při 11 nebo méně.",
                        "Eso + šestka (soft 17) je výhodná ruka — můžeš bezpečně brát další kartu.",
                        "Pokud má bankovník 4–6, hráč může být agresivnější — bankovník má velkou šanci na bust."
                ),
                List.of(
                        "Basic strategy: stand on 17 or more; hit on 11 or less.",
                        "Ace + six (soft 17) is a good hand — you can safely take another card.",
                        "If the dealer shows 4–6, be more aggressive — the dealer has a high bust probability."
                )
        );

        seed("KNIFFEL", "🎲🎲",
                "Kniffel (Yahtzee)", "Kniffel (Yahtzee)",
                "5 kostek — plň kombinace a nasbírej co nejvíce bodů", "5 dice — fill categories and score as many points as possible",
                "Kniffel (Yahtzee) je kostkovaná hra pro 2–6 hráčů. Každý hráč má v každém kole až 3 hody pěti kostkami. Po každém kole zapíše výsledek do jedné z 13 kategorií. Kdo nasbíral nejvíce bodů, vyhrává.",
                "Kniffel (Yahtzee) is a dice game for 2–6 players. Each player has up to 3 rolls of five dice per round, then records a score in one of 13 categories. Highest total score wins.",
                "Zaplň všech 13 kategorií. Hráč s nejvyšším celkovým skóre vyhrává. Kniffel (pět stejných) přináší 50 bodů.",
                "Fill all 13 categories on your score sheet. Highest total wins. Kniffel (five of a kind) scores 50 points.",
                List.of(
                        "Každý hráč má v kole až 3 hody — po prvním hodu může libovolné kostky odložit a přehodit zbylé.",
                        "Po třetím hodu se výsledek zapíše do jedné kategorie.",
                        "Horní sekce (1–6): součet pouze dané hodnoty. Bonus 35 bodů za součet ≥ 63.",
                        "Dolní sekce: trojka, čtveřice, full house (25 b.), malá ulice (30 b.), velká ulice (40 b.), chance, Kniffel (50 b.).",
                        "Každou kategorii lze použít pouze jednou; prázdná kategorie se škrtne za 0 bodů.",
                        "Hráč s nejvyšším celkovým skóre vyhrává."
                ),
                List.of(
                        "Each player gets up to 3 rolls per round — after the first roll, set aside any dice and re-roll the rest.",
                        "After the third roll, record a score in one of the 13 categories.",
                        "Upper section (1–6): sum of that number only. Bonus 35 points for a total of ≥ 63.",
                        "Lower section: three-of-a-kind, four-of-a-kind, full house (25), small straight (30), large straight (40), chance, Kniffel (50).",
                        "Each category may only be used once; an unused category is crossed out for 0 points.",
                        "Highest total score wins."
                ),
                List.of(
                        "Zachovej si Kniffel a velkou ulici na pozdější kola — jsou nejtěžší ke splnění.",
                        "V horní sekci se snaž mít průměr alespoň 3× hodnotu na každém čísle.",
                        "Chance je záchranná kategorie — používej ji, když ti nevychází nic jiného."
                ),
                List.of(
                        "Save Kniffel and large straight for later — they are the hardest to complete.",
                        "In the upper section, aim for at least 3× the face value on each number.",
                        "Chance is your rescue category — use it when nothing else fits."
                )
        );

        seed("GOMOKU", "⚫⚪",
                "Piškvorky (Gomoku)", "Gomoku (Five in a Row)",
                "Pět v řadě — na papíře nebo desce", "Five in a row — on paper or a board",
                "Piškvorky (Gomoku) jsou strategická hra pro dva hráče. Na poli 15×15 hráči střídavě zakreslují své symboly. Cílem je sestavit pět vlastních symbolů v řadě — vodorovně, svisle nebo diagonálně.",
                "Gomoku (Five in a Row) is a strategy game for two players. On a 15×15 board players alternate placing their symbols. The goal is to connect five of your own symbols in a row — horizontally, vertically, or diagonally.",
                "Sestav jako první pět svých symbolů v přímé řadě. Na standardním poli 15×15 platí pravidlo 'přesně 5' — šestice nebo více se nepočítají.",
                "Be the first to place five of your symbols in a row. On a standard 15×15 board the 'exactly 5' rule applies — six or more in a row does not count.",
                List.of(
                        "Hráči se střídají; každý zaznamená svůj symbol na jedno volné pole.",
                        "Kdo sestaví pět symbolů za sebou (vodorovně, svisle nebo diagonálně), vyhrává.",
                        "Na poli 15×15 platí pravidlo 'přesně 5' — šestice nebo více se nepočítají.",
                        "Volitelné pravidlo swap2: černý hráč položí 2 černé a 1 bílý kámen; bílý zvolí, zda vyměnit barvy."
                ),
                List.of(
                        "Players alternate; each places their symbol on one empty cell.",
                        "The first to connect five of their symbols in a row wins.",
                        "On a 15×15 board the 'exactly five' rule applies — six or more in a row does not count.",
                        "Optional swap2 rule: the first player places 2 black and 1 white stone; the second player chooses which colour to take."
                ),
                List.of(
                        "Útočíš-li ze dvou stran najednou (otevřená čtveřice), soupeř nemůže zablokovat obě.",
                        "Kontroluj střed desky — má nejvíce sousedních polí pro expanzi.",
                        "Hraj trojice a čtveřice na různých liniích současně."
                ),
                List.of(
                        "A two-sided open four cannot be blocked — create one whenever you can.",
                        "Control the centre of the board — it has the most adjacent cells for expansion.",
                        "Build threats on multiple lines simultaneously so your opponent cannot block them all."
                )
        );

        seed("BADMINTON", "🏸",
                "Badminton", "Badminton",
                "Rakety a košíček — první na 21 bodů", "Rackets and shuttlecock — first to 21 points",
                "Badminton je dvorceová hra pro 2 hráče (nebo 2 páry). Hráči odpalují košíček přes síť raketami. Košíček nesmí dopadnout na zem na straně odpalujícího. Hra je extrémně rychlá a náročná na pohyb.",
                "Badminton is a racket sport for 2 players (or 2 pairs). Players hit a shuttlecock back and forth over a net. The shuttlecock must not land on the ground on the hitter's side. The game is extremely fast and physically demanding.",
                "Vyhraj set ziskem 21 bodů s minimálně 2bodovým náskokem (maximum 30:29). Zápas se hraje na 2 vítězné sety ze 3.",
                "Win a set by reaching 21 points with at least a 2-point lead (capped at 30:29). A match is best of 3 sets.",
                List.of(
                        "Podání: oba hráči musí stát ve svých polích; košíček se smí odehrát pouze pod výší pasu.",
                        "Bod se hraje při každém podání (rally-point systém).",
                        "Košíček nesmí dopadnout na zem na straně odpalujícího.",
                        "Set vyhrává hráč s 21 body (2bodový náskok); při 29:29 rozhoduje 30. bod.",
                        "Při výhře setu si hráči mění strany; při třetím setu se mění při 11 bodech."
                ),
                List.of(
                        "Service: both players must stand in their service courts; the shuttle must be hit below waist height.",
                        "A point is played on every serve (rally-point system).",
                        "The shuttle must not land on the ground on the hitter's side.",
                        "A set is won with 21 points (2-point lead); at 29–29 the 30th point decides.",
                        "Players switch ends after each set; in the third set they switch at 11 points."
                ),
                List.of(
                        "Smash je nejúčinnější úder — ale vyžaduje dobrou pozici pod košíčkem.",
                        "Drop shot (jemný úder těsně za síť) nutí soupeře rychle dopředu.",
                        "Vracuj se vždy do středu kurtu po každém odehrání."
                ),
                List.of(
                        "The smash is the most powerful shot — but requires good positioning under the shuttle.",
                        "A drop shot (soft shot just over the net) forces your opponent to rush forward.",
                        "Always return to the centre of the court after each shot."
                )
        );

        seed("LIAR_DICE", "🎲🤥",
                "Lhářovy kostky", "Liar's Dice",
                "Bluffuj nebo odhal lháře — kdo přijde o všechny kostky, prohrává", "Bluff or call the liar — lose all your dice and you're out",
                "Lhářovy kostky jsou bluffovací hra pro 2–6 hráčů. Každý hráč hodí 5 kostkami a skryje je. Hráči střídavě zvyšují bid — říkají, kolik kostek s danou hodnotou leží celkem na stole. Kdo si myslí, že hráč lže, vyzve ho. Chybující hráč ztrácí kostku.",
                "Liar's Dice is a bluffing game for 2–6 players. Each player rolls 5 dice and hides them. Players take turns raising the bid — stating how many dice on the table show a given value. If you think a player is lying, challenge them. The mistaken player loses a die.",
                "Přichyť soupeře při lhaní nebo bluffuj tak přesvědčivě, aby ostatní nevyzývali. Kdo přijde o všechny kostky, vypadá. Poslední hráč s kostkami vyhrává.",
                "Catch your opponent lying or bluff convincingly enough that others don't challenge you. Lose all your dice and you are eliminated. Last player with dice wins.",
                List.of(
                        "Každý hráč hodí 5 kostkami a skryje je pod pohár.",
                        "Hráči střídavě navyšují bid: říkají 'X kostek ukazuje hodnotu Y' (musí navýšit počet nebo hodnotu).",
                        "Hráč může kdykoliv vyzvat předchozího hráče ('lžeš!'): všichni odkryjí kostky.",
                        "Pokud bid byl pravdivý, vyzývající ztrácí kostku.",
                        "Pokud bid byl nepravdivý, hráč, který ho řekl, ztrácí kostku.",
                        "Eso (1) je divoká karta — počítá se jako jakákoliv hodnota (volitelné pravidlo).",
                        "Kdo přijde o všechny kostky, vypadá. Poslední zbývající hráč vyhrává."
                ),
                List.of(
                        "Each player rolls 5 dice and hides them under a cup.",
                        "Players alternate raising the bid: state 'X dice showing value Y' (must increase quantity or face value).",
                        "Any player may challenge the previous bid ('liar!'): everyone reveals their dice.",
                        "If the bid was true, the challenger loses a die.",
                        "If the bid was false, the bidder loses a die.",
                        "Ones (aces) are wild — they count as any value (optional rule).",
                        "Lose all your dice and you are out. Last player remaining wins."
                ),
                List.of(
                        "Zapamatuj si své vlastní kostky a od nich odvoď pravděpodobnost celého stolu.",
                        "Bluffuj agresivně na začátku kola, kdy má soupeř nejméně informací.",
                        "Sleduj, kdy soupeř váhá při navýšení — pravděpodobně bluffuje."
                ),
                List.of(
                        "Remember your own dice and use them to estimate the probability for the whole table.",
                        "Bluff aggressively at the start of a round when opponents have the least information.",
                        "Watch for hesitation when an opponent raises the bid — they are probably bluffing."
                )
        );

        seed("FOOTBALL", "⚽",
                "Fotbal", "Football (Soccer)",
                "Klasický fotbal — kdo dá více gólů, vyhrává", "Classic football — most goals wins",
                "Fotbal je nejpopulárnější sport na světě. Hraje se mezi dvěma týmy (nebo hráči v malé formě 1v1, 2v2, 3v3, 5v5). Cílem je vstřelit soupeři více branek za dohodnutou dobu nebo do dohodnutého počtu gólů.",
                "Football (soccer) is the world's most popular sport. It is played between two teams (or players in small formats: 1v1, 2v2, 3v3, 5v5). The goal is to score more goals than the opponent in the agreed time or to an agreed score.",
                "Vstřel soupeři více branek za dohodnutou dobu nebo jako první dosáhni dohodnutého počtu gólů. Při remíze se hraje prodloužení nebo penaltový rozstřel.",
                "Score more goals than your opponent in the agreed time, or be the first to reach an agreed number of goals. If tied, play extra time or a penalty shootout.",
                List.of(
                        "Dohodněte se předem na formátu: čas, počet gólů nebo počet hráčů na stranu.",
                        "Hráči smí hrát míč libovolnou částí těla kromě rukou a paží.",
                        "Faul: záměrné kopnutí soupeře, hold nebo hra rukou = volný kop nebo penalta.",
                        "Ofsajd platí ve standardním fotbale; při malé kopané se většinou nehraje.",
                        "Remíza: dle dohody prodloužení nebo penaltový rozstřel."
                ),
                List.of(
                        "Agree on the format beforehand: duration, number of goals, or players per side.",
                        "Players may use any part of the body except the arms and hands.",
                        "Foul: deliberate kicking, holding, or handball = free kick or penalty.",
                        "Offside applies in standard football; it is usually not played in small-sided games.",
                        "If tied: agree on extra time or a penalty shootout."
                ),
                List.of(
                        "Komunikace je klíčová — volej se spoluhráči a oznamuj svou pozici.",
                        "Bez míče se pohybuj do volného prostoru a nabízej se.",
                        "Při 1v1 drž míč blíže a hraj s těžištěm níže."
                ),
                List.of(
                        "Communication is key — call for the ball and announce your position.",
                        "Off the ball, move into space and make yourself available.",
                        "In a 1v1, keep the ball close and lower your centre of gravity."
                )
        );

        seed("BASKETBALL", "🏀",
                "Basketbal", "Basketball",
                "Košíkovaná — kdo dá více košů, vyhrává", "Most points wins — shoot the ball in the basket",
                "Basketbal je sport pro 5 hráčů na stranu (nebo 3v3 ve street basketball). Hráči driblují míčem a snaží se ho hodit do soupeřova koše. Kdo nasbírá více bodů za dohodnutou dobu, vyhrává.",
                "Basketball is a sport for 5 players per side (or 3v3 in street basketball). Players dribble the ball and try to shoot it into the opponent's basket. The team with more points at the end of the agreed time wins.",
                "Nasbírej více bodů za dohodnutou dobu. Koš z pole = 2 body; za trojkovou čárou = 3 body; trestný hod = 1 bod.",
                "Score more points than your opponent in the agreed time. Field goal = 2 points; behind the arc = 3 points; free throw = 1 point.",
                List.of(
                        "Hráč pohybující se s míčem musí driblovat — jinak je to chůze.",
                        "Koš za trojkovou čárou = 3 body; ostatní koše = 2 body; trestný hod = 1 bod.",
                        "Faul: neoprávněný tělesný kontakt. Po 5 osobních chybách hráč opouští hru.",
                        "Trestné hody se udělují za faulování ve střeleckém pohybu.",
                        "Míč mimo hřiště: kdo se ho naposledy dotkl, ztrácí míč.",
                        "Dohodněte se předem na formátu: délka poločasů, pravidlo shot clock, trojková čára."
                ),
                List.of(
                        "A player moving with the ball must dribble — otherwise it is a travelling violation.",
                        "Shot behind the arc = 3 points; other field goals = 2 points; free throw = 1 point.",
                        "Foul: illegal physical contact. After 5 personal fouls a player leaves the game.",
                        "Free throws are awarded for fouls committed during a shooting action.",
                        "Out of bounds: the team that last touched the ball loses possession.",
                        "Agree on the format in advance: half length, shot clock rule, three-point line."
                ),
                List.of(
                        "V 1v1 využij crossover dribling — mění směr útoku a mate soupeře.",
                        "Střílej s vypnutým zápěstím a vztyčeným ukazovákem — follow-through zajistí rotaci.",
                        "Hráj bez míče aktivně — nabízej se na průnikové přihrávky."
                ),
                List.of(
                        "In 1v1, use the crossover dribble — it changes your attack direction and wrong-foots your opponent.",
                        "Shoot with a full wrist snap and a raised index finger — the follow-through puts backspin on the ball.",
                        "Stay active off the ball — make yourself available for give-and-go passes."
                )
        );

        seed("FARKLE", "🎲",
                "Kostky (Farkle)", "Farkle (Dice)",
                "Hraj a sbírej body — ale nebud příliš chamtivý!", "Roll and score — but don't get greedy!",
                "Farkle je kostkovaná hra pro 2–8 hráčů se šesti kostkami. Hráč hází kostkami, odkládá bodující kombinace a rozhoduje, zda bude házel dál (riskuje ztrátu bodů z kola) nebo si body připíše. Cílem je jako první dosáhnout 10 000 bodů.",
                "Farkle is a dice game for 2–8 players using six dice. On their turn a player rolls, sets aside scoring combinations, and decides whether to keep rolling (risking a Farkle and losing the round's points) or bank the points. First to 10 000 wins.",
                "Jako první dosáhni celkového skóre 10 000 bodů. Poté mají všichni ostatní hráči ještě jedno kolo, aby tě překonali.",
                "Be the first to reach a running total of 10 000 points. Once you do, every other player gets one final turn to beat your score.",
                List.of(
                        "Hráč hodí všemi šesti kostkami.",
                        "Musí odložit alespoň jednu bodující kostku/kombinaci.",
                        "Pokud žádná kostka neboduje = Farkle — hráč ztrácí veškeré body z aktuálního kola.",
                        "Bodující kombinace: jedničky = 100 b. každá; pětky = 50 b. každá; trojice stejných = hodnota × 100 (trojice jedniček = 1 000 b.); čtveřice = trojice × 2; pětice = čtveřice × 2; šestice = pětice × 2; supr hra (1–2–3–4–5–6 v jednom hodu) = 1 500 b.; tři páry = 1 500 b.",
                        "Odloží-li hráč všechny kostky jako bodující, může je hodit znovu všechny (hot dice).",
                        "Hráč může kdykoli po odložení bodující kostky zastavit a připsat si body z kola.",
                        "Do hry vstoupí hráč, až nasbírá alespoň 500 bodů v jednom kole (volitelné pravidlo).",
                        "Po dosažení 10 000 bodů mají ostatní hráči ještě jedno finální kolo."
                ),
                List.of(
                        "The active player rolls all six dice.",
                        "They must set aside at least one scoring die or combination.",
                        "If no dice score = Farkle — the player loses all points accumulated this turn.",
                        "Scoring: ones = 100 pts each; fives = 50 pts each; three-of-a-kind = face value × 100 (three 1s = 1 000); four-of-a-kind = triple score × 2; five-of-a-kind = four-of-a-kind × 2; six-of-a-kind = five-of-a-kind × 2; straight (1–2–3–4–5–6 in one roll) = 1 500 pts; three pairs = 1 500 pts.",
                        "If all six dice score, the player may pick them all up and roll again (hot dice).",
                        "After setting aside at least one scoring die the player may stop and bank their round total.",
                        "A player must score at least 500 points in a single turn to get on the scoreboard (optional rule).",
                        "Once a player reaches 10 000, all remaining players get one last turn to beat the score."
                ),
                List.of(
                        "Vezmi vždy jedničky a pětky — jsou nejspolehlivější bodující kostky.",
                        "Nezastavuj pod 300 body v kole — riziko není stálo za tak malý zisk.",
                        "Hot dice jsou příležitost — 6 kostek znovu znamená šanci na velký skok."
                ),
                List.of(
                        "Always keep ones and fives first — they are the most reliable scorers.",
                        "Don't bank below 300 points in a turn — the risk is not worth the small gain.",
                        "Hot dice are your opportunity — rerolling all six means a chance for a big score jump."
                )
        );

        seed("CHESS", "♟️",
                "Šachy", "Chess",
                "Klasická strategická desková hra", "The classic strategy board game",
                "Šachy jsou strategická desková hra pro dva hráče na šachovnici 8×8. Každý hráč začíná se 16 figurkami: král, dáma, dvě věže, dva střelci, dva jezdci a osm pěšáků. Cílem je dát soupeřovu králi mat — postavit ho do šachu bez možnosti úniku.",
                "Chess is a two-player strategy board game played on an 8×8 board. Each player starts with 16 pieces: one king, one queen, two rooks, two bishops, two knights, and eight pawns. The objective is to checkmate the opponent's king — put it in check with no legal escape.",
                "Dej soupeřovu králi mat: pozici, kde je král v šachu a nemá žádný legální tah k úniku. Hra může skončit remízou pat, trojím opakováním pozice, nedostatečným materiálem nebo dohodou.",
                "Put your opponent's king in checkmate: a position where the king is in check and has no legal move to escape. The game can also end in a draw by stalemate, threefold repetition, insufficient material, or mutual agreement.",
                List.of(
                        "Bílý táhne jako první; hráči se střídají.",
                        "Král táhne o jedno pole libovolným směrem. Nesmí vstoupit do šachu.",
                        "Dáma táhne libovolný počet polí libovolným směrem.",
                        "Věž táhne libovolný počet polí vodorovně nebo svisle.",
                        "Střelec táhne libovolný počet polí diagonálně (zůstává na své barvě).",
                        "Jezdec táhne ve tvaru L: dvě pole jedním směrem, pak jedno pole kolmo. Jako jediná figurka přeskakuje ostatní.",
                        "Pěšák táhne o jedno pole dopředu (o dvě pole z výchozí pozice) a bere diagonálně.",
                        "Speciální tahy: rošáda (král + věž), en passant (brání pěšáka), povýšení pěšáka (na poslední řadě se mění na jakoukoli figurku).",
                        "Hráč v šachu musí z šachu uniknout svým dalším tahem — nemůže ho ignorovat."
                ),
                List.of(
                        "White moves first; players alternate turns.",
                        "King moves one square in any direction. It cannot move into check.",
                        "Queen moves any number of squares in any direction.",
                        "Rook moves any number of squares along a row or column.",
                        "Bishop moves any number of squares diagonally (stays on its starting colour).",
                        "Knight moves in an L-shape: two squares one way, then one square perpendicular. It is the only piece that can jump over others.",
                        "Pawn moves one square forward (two on its first move) and captures diagonally.",
                        "Special moves: castling (king + rook), en passant (pawn capture), and pawn promotion (reaching the last rank).",
                        "A player in check must escape it on their next move — they cannot ignore it."
                ),
                List.of(
                        "Kontroluj střed pěšáky a figurkami v zahájení.",
                        "Vyvin jezdce a střelce před dalšími tahy stejnou figurkou.",
                        "Roštujte brzy pro ochranu krále a propojení věží.",
                        "Vyměňuj figurky, pokud jsi v materiální výhodě; vyhýbej se výměnám při nevýhodě.",
                        "Vždy nejdřív zvaž hrozby soupeře, než uděláš vlastní tah."
                ),
                List.of(
                        "Control the centre with pawns and pieces early in the game.",
                        "Develop your knights and bishops before moving the same piece twice.",
                        "Castle early to protect your king and connect your rooks.",
                        "Trade pieces when you are ahead in material; avoid trades when behind.",
                        "Always consider your opponent's threats before making your own move."
                )
        );

        seed("UNO", "🃏🌈",
                "UNO", "UNO",
                "Zbav se všech karet jako první — nezapomeň říct UNO!", "Get rid of all your cards first — don't forget to say UNO!",
                "UNO je oblíbená karetní hra pro 2–10 hráčů se speciálním balíčkem 108 karet. Hráči se snaží co nejrychleji zbavit všech svých karet přikládáním karet stejné barvy nebo hodnoty na odkladiště. Akční karty (Přeskoč, Ber 2, Otočení, Divoká) přidávají dramatičnost a náhlé zvraty — a kdo mu zbyde poslední karta, musí hlasitě zvolat 'UNO!'",
                "UNO is a popular card game for 2–10 players with a special 108-card deck. Players race to empty their hand by matching the top discard by colour or number. Action cards (Skip, Draw Two, Reverse, Wild) create drama and sudden swings — and the player left with one card must loudly call 'UNO!'",
                "Jako první se zbav všech svých karet. Předposlední kartu oznam hlasitým 'UNO!' — zapomeneš-li a soupeř si toho všimne dříve, než zahraješ další tah, bereš 2 trestné karty.",
                "Be the first to empty your hand. When playing your second-to-last card, call 'UNO!' loudly — if you forget and an opponent catches you before your next turn, draw 2 penalty cards.",
                List.of(
                        "Každý hráč dostane 7 karet; vrchní karta balíčku se odkryje jako startovní odkladiště.",
                        "Na tahu přilož kartu stejné barvy nebo hodnoty jako vrchní karta odkladiště — nebo zahraj speciální kartu.",
                        "Nemáš-li co přiložit, dobereš 1 kartu z balíčku; pokud ji můžeš zahrát, smíš ji zahrát okamžitě, jinak tah končí.",
                        "Přeskoč: příští hráč ztrácí tah. Otočení: u 2 hráčů funguje jako Přeskoč. Ber 2: příštíhráč bere 2 karty a přeskočí tah.",
                        "Divoká karta: zahrát na cokoli, hráč zvolí novou barvu.",
                        "Divoká Ber 4: smíš zahrát pouze pokud nemáš kartu aktuální barvy; příštíhráč bere 4 karty a přeskočí tah; soupeř může hru zpochybnit.",
                        "Zbývá-li ti 1 karta, zvolej 'UNO!' — zapomeneš-li a soupeř si toho všimne před svým dalším tahem, bereš 2 karty navíc.",
                        "Kdo jako první vyloží všechny karty, vyhrává kolo."
                ),
                List.of(
                        "Each player gets 7 cards; the top card of the draw pile is turned face-up to start the discard pile.",
                        "On your turn play a card matching the top discard by colour or number — or play a special card.",
                        "If you cannot play, draw one card; if it is playable you may play it immediately, otherwise your turn ends.",
                        "Skip: next player loses a turn. Reverse: with 2 players acts as Skip. Draw Two: next player draws 2 cards and loses a turn.",
                        "Wild: may be played on any card; the player then names the new colour.",
                        "Wild Draw Four: may only be played when you have no card of the current colour; next player draws 4 and loses a turn; opponent may challenge.",
                        "When you have 1 card left, call 'UNO!' — forget and an opponent catches you before their next turn, draw 2 penalty cards.",
                        "The first player to play their last card wins the round."
                ),
                List.of(
                        "Drž si akční karty (Ber 2, Divoká Ber 4) na krizové situace, kdy soupeřovi zbývá málo karet.",
                        "Při hraní Divoké karty přepni na barvu, které máš v ruce nejvíce.",
                        "Sleduj počet karet soupeře — jakmile volá UNO, zasypej ho akcemi a přimej ho brát."
                ),
                List.of(
                        "Save action cards (Draw Two, Wild Draw Four) for when your opponent is close to winning.",
                        "When playing a Wild, switch to the colour you hold the most of.",
                        "Watch your opponent's hand size — once they call UNO, hit them with action cards to force draws."
                )
        );

        seed("ZOLIKY", "🃏🃏",
                "Žolíky", "Žolíky (Rummy with Jokers)",
                "Sestavuj skupiny a postupky — kdo se zbaví karet první, vyhrává", "Form sets and runs — first to empty their hand wins",
                "Žolíky jsou česká karetní hra pro 2–4 hráče se standardním balíčkem 54 karet (52 + 2 žolíky). Hráči střídavě dobírají a odkládají karty a snaží se sestavit skupiny (3–4 karty stejné hodnoty různých barev) a postupky (3 a více karet stejné barvy v pořadí). Žolík (joker) zastupuje libovolnou kartu.",
                "Žolíky is a Czech card game for 2–4 players using a standard 54-card deck (52 + 2 jokers). Players take turns drawing and discarding, trying to form sets (3–4 cards of the same rank in different suits) and runs (3+ consecutive cards of the same suit). A joker substitutes any card.",
                "Jako první rozlož všechny karty do platných skupin a postupků a odhoď poslední kartu na odkladiště. Kdo se jako první zbaví celé ruky, vyhrává kolo.",
                "Be the first to meld all your cards into valid sets and runs and discard the final card. The first player to empty their hand wins the round.",
                List.of(
                        "Každý hráč dostane 13 karet (při 2 hráčích) nebo 10 karet (3–4 hráči); zbytek tvoří dobírací balíček s jednou odkrytou kartou jako odkladiště.",
                        "Na tahu dobereš 1 kartu — buď z vrcholu balíčku, nebo z vrcholu odkladiště.",
                        "Skupiny: 3 nebo 4 karty stejné hodnoty různých barev (např. tři sedmičky).",
                        "Postupky: 3 nebo více karet stejné barvy v pořadí (např. 5–6–7 srdcová). Eso může být nejvyšší (Q–K–A) i nejnižší (A–2–3).",
                        "Hotové kombinace (skupiny nebo postupky) položíš na stůl před sebe — soupeři je vidí.",
                        "Na již ležící kombinace (své i soupeřovy) smíš přikládat pasující karty.",
                        "Žolík zastupuje libovolnou kartu v kombinaci; z položené kombinace ho nelze vzít.",
                        "Tah končí odložením 1 karty na odkladiště. Kdo vyloží vše a odhodí poslední kartu, vyhrává."
                ),
                List.of(
                        "Each player gets 13 cards (2 players) or 10 cards (3–4 players); the rest form a draw pile with one card face-up as the discard pile.",
                        "On your turn draw 1 card — either from the top of the draw pile or the top of the discard pile.",
                        "Sets: 3 or 4 cards of the same rank in different suits (e.g. three sevens).",
                        "Runs: 3 or more consecutive cards of the same suit (e.g. 5–6–7 of hearts). Ace can be high (Q–K–A) or low (A–2–3).",
                        "Completed melds (sets or runs) are placed face-up on the table in front of you.",
                        "You may add matching cards to any meld already on the table — yours or your opponent's.",
                        "A joker substitutes any card in a meld; once placed on the table it cannot be taken.",
                        "End your turn by discarding 1 card. The first player to table all cards and discard the last one wins."
                ),
                List.of(
                        "Beri z odkladiště pouze tehdy, pokud ta karta okamžitě dokončí kombinaci — jinak odhaluješ soupeři svůj plán.",
                        "Žolíka neplýtvej na malé kombinace — šetři ho na těžko sestavitelnou postupku nebo rozhodující skupinu.",
                        "Sleduj, co soupeř odkládá — poznáš, jaké barvy nebo hodnoty nepotřebuje, a přizpůsobíš strategii."
                ),
                List.of(
                        "Only take from the discard pile if that card immediately completes a meld — otherwise you reveal your strategy.",
                        "Don't waste a joker on a small meld — save it for a run that is hard to complete naturally.",
                        "Watch what your opponent discards — you will learn which suits or ranks they do not need and adapt your strategy."
                )
        );

        seed("PUB_QUIZ", "🧠❓",
                "Hospodský kvíz", "Pub Quiz",
                "10 otázek — kdo ví víc, vyhrává mozkem", "10 questions — outsmart your opponent",
                "Hospodský kvíz je hra vědomostí pro 2 hráče. Jeden hráč pokládá otázky z telefonu nebo vlastní hlavy, druhý odpovídá — pak se role vymění. Alternativně oba odpovídají nezávisle na stejné otázky z neutrálního zdroje. Kdo odpoví správně na více otázek, vyhrává. Žádné vybavení není potřeba — stačí telefon nebo dobrá paměť.",
                "Pub Quiz is a knowledge game for two players. One player asks questions from their phone or memory, the other answers — then roles swap. Alternatively, both answer the same questions independently from a neutral source. The player with more correct answers wins. No equipment needed — just a phone or a good memory.",
                "Kdo odpoví správně na více otázek z dohodnutého počtu (doporučeno 10), vyhrává. Při remíze rozhoduje náhlá smrt — jedna otázka, kdo odpoví první a správně.",
                "The player with more correct answers out of the agreed total (10 recommended) wins. On a tie, sudden death applies — one question, first correct answer wins.",
                List.of(
                        "Dohodněte se na počtu otázek (standard: 10) a případně tématu (obecné znalosti, sport, film, hudba, zeměpis…).",
                        "Formát A — Examinátor: jeden hráč pokládá otázky (z trivia aplikace, Wikipedie nebo vlastní hlavy), druhý odpovídá; po 5 otázkách se role vymění.",
                        "Formát B — Simultánní: oba hráči odpovídají nezávisle na stejné otázky z neutrálního zdroje (trivia web, kvízová aplikace), pak porovnají výsledky.",
                        "Časový limit na odpověď: doporučeno 30 sekund — lze dohodnout jinak.",
                        "Správná odpověď = 1 bod. Přibližná nebo neúplná odpověď = diskuze, případně půl bodu.",
                        "Po 10 otázkách sečtěte body. Při remíze: jedna otázka náhlou smrtí — kdo odpoví první a správně, vyhrává."
                ),
                List.of(
                        "Agree on the number of questions (standard: 10) and optionally the topic (general knowledge, sport, film, music, geography…).",
                        "Format A — Examiner: one player asks questions (from a trivia app, Wikipedia, or memory), the other answers; swap roles after 5 questions.",
                        "Format B — Simultaneous: both players answer the same questions independently from a neutral source (trivia website, quiz app), then compare.",
                        "Time limit per question: 30 seconds recommended — agree beforehand.",
                        "Correct answer = 1 point. Partial or approximate answers — discuss and optionally award half a point.",
                        "After 10 questions tally the scores. On a tie: sudden death — one question, first correct answer wins."
                ),
                List.of(
                        "Buď přesný v otázkách — vyhni se nejednoznačným formulacím, aby nedocházelo ke sporům o správnost odpovědi.",
                        "V simultánním formátu si odpovědi napište tajně před odhalením — zabraňuje cheatu.",
                        "Střídej témata, ve kterých jsi silný, s obtížnějšími — udržíš tempo a soupeře překvapíš."
                ),
                List.of(
                        "Keep questions precise — avoid ambiguous phrasing to prevent disputes over what counts as correct.",
                        "In simultaneous format, write your answers secretly before revealing — it prevents cheating.",
                        "Mix topics you are strong in with harder ones — it keeps momentum and catches your opponent off guard."
                )
        );
    }

    private void seed(String key, String icon,
                      String nameCs,        String nameEn,
                      String taglineCs,     String taglineEn,
                      String descriptionCs, String descriptionEn,
                      String howToWinCs,    String howToWinEn,
                      List<String> rulesCs, List<String> rulesEn,
                      List<String> tipsCs,  List<String> tipsEn) {
        if (!gameRepository.existsByKey(key)) {
            gameRepository.save(new Game(key, icon,
                    nameCs, nameEn, taglineCs, taglineEn,
                    descriptionCs, descriptionEn, howToWinCs, howToWinEn,
                    rulesCs, rulesEn, tipsCs, tipsEn));
        }
    }
}
