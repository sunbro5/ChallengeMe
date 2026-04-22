<template>
  <div class="pp-page">
    <div class="pp-wrap">
      <button class="back-btn" @click="$router.back()">{{ $t('common.back') }}</button>

      <!-- CS -->
      <template v-if="$i18n.locale === 'cs'">
        <h1>Zásady ochrany osobních údajů · ChallengeMe</h1>
        <p class="updated">Poslední aktualizace: duben 2026</p>

        <section class="pp-section">
          <h2>1. Správce osobních údajů</h2>
          <p>
            Správcem osobních údajů ve smyslu čl. 4 odst. 7 GDPR je provozovatel
            platformy ChallengeMe. Kontaktní e-mail: <strong>[email]</strong>.
          </p>
        </section>

        <section class="pp-section">
          <h2>2. Jaké osobní údaje zpracováváme</h2>
          <table class="pp-table">
            <thead>
              <tr><th>Kategorie</th><th>Konkrétní údaje</th><th>Účel</th></tr>
            </thead>
            <tbody>
              <tr>
                <td>Účet</td>
                <td>Uživatelské jméno, e-mailová adresa, rok narození, zahashované heslo</td>
                <td>Registrace a správa účtu</td>
              </tr>
              <tr>
                <td>Aktivita</td>
                <td>Herní statistiky (výhry, prohry, remízy, rating), herní výzvy, výsledky</td>
                <td>Provoz žebříčku a funkce výzev</td>
              </tr>
              <tr>
                <td>Poloha</td>
                <td>GPS souřadnice výzvy (zadané při vytváření výzvy)</td>
                <td>Zobrazení výzev na mapě</td>
              </tr>
              <tr>
                <td>Komunikace</td>
                <td>Obsah zpráv v chatu platformy</td>
                <td>Komunikace mezi uživateli</td>
              </tr>
              <tr>
                <td>Bezpečnost</td>
                <td>Záznamy hlášení (reportů) jiných uživatelů</td>
                <td>Moderace a bezpečnost platformy</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="pp-section">
          <h2>3. Právní základ zpracování</h2>
          <ul>
            <li><strong>Plnění smlouvy (čl. 6 odst. 1 písm. b GDPR)</strong> — zpracování nezbytné pro provoz účtu a funkcí platformy (výzvy, chat, žebříček).</li>
            <li><strong>Oprávněný zájem (čl. 6 odst. 1 písm. f GDPR)</strong> — zajištění bezpečnosti platformy, prevence zneužití a moderace obsahu.</li>
          </ul>
        </section>

        <section class="pp-section">
          <h2>4. Cookies</h2>
          <p>Používáme dva typy cookies:</p>
          <table class="pp-table">
            <thead>
              <tr><th>Typ</th><th>Příklad</th><th>Účel</th><th>Souhlas</th></tr>
            </thead>
            <tbody>
              <tr>
                <td><strong>Nezbytné</strong></td>
                <td>Session cookie (HttpOnly)</td>
                <td>Přihlášení a bezpečnost</td>
                <td>Nevyžadován</td>
              </tr>
              <tr>
                <td><strong>Analytické</strong></td>
                <td>Google Analytics (_ga, _gid)</td>
                <td>Anonymní statistiky návštěvnosti</td>
                <td>Vyžadován — udělíš při příchodu na web</td>
              </tr>
            </tbody>
          </table>
          <p>
            Analytické cookies jsou aktivovány <strong>pouze pokud s nimi souhlasíš</strong>
            prostřednictvím lišty na webu. Souhlas lze kdykoli odvolat vymazáním
            localStorage v nastavení prohlížeče nebo kliknutím na odkaz „Nastavení cookies"
            v zápatí stránek.
          </p>
          <p>
            Google Analytics zpracovává data anonymizovaně (IP anonymizace zapnuta).
            Více informací: <a href="https://policies.google.com/privacy" target="_blank" rel="noopener">Zásady ochrany soukromí Google</a>.
          </p>
        </section>

        <section class="pp-section">
          <h2>5. Příjemci osobních údajů</h2>
          <p>
            Osobní údaje <strong>neprodáváme ani nesdílíme</strong> s třetími stranami za
            marketingovými účely. Data jsou uložena na serverech provozovatele a nejsou
            předávána mimo Evropský hospodářský prostor.
          </p>
          <p>
            Uživatelské jméno a herní statistiky jsou v rámci platformy viditelné ostatním
            přihlášeným uživatelům (žebříček, profil hráče).
          </p>
        </section>

        <section class="pp-section">
          <h2>6. Doba uchovávání</h2>
          <ul>
            <li><strong>Data účtu</strong> — po dobu trvání účtu. Po smazání účtu jsou nenávratně odstraněna.</li>
            <li><strong>Herní záznamy</strong> — po dobu trvání účtu.</li>
            <li><strong>Zprávy chatu</strong> — po dobu trvání účtu nebo do smazání konverzace.</li>
            <li><strong>Záznamy hlášení</strong> — uchovávány pro účely bezpečnosti po dobu nezbytně nutnou.</li>
          </ul>
        </section>

        <section class="pp-section">
          <h2>7. Vaše práva (GDPR)</h2>
          <ul>
            <li><strong>Právo na přístup (čl. 15)</strong> — můžete požádat o přehled zpracovávaných údajů.</li>
            <li><strong>Právo na opravu (čl. 16)</strong> — nesprávné údaje lze opravit v nastavení profilu.</li>
            <li><strong>Právo na výmaz (čl. 17)</strong> — svůj účet a veškerá data smažete přes <em>Profil → Smazat účet</em>.</li>
            <li><strong>Právo na přenositelnost (čl. 20)</strong> — na vyžádání poskytneme data ve strojově čitelném formátu.</li>
            <li><strong>Právo vznést námitku (čl. 21)</strong> — proti zpracování na základě oprávněného zájmu.</li>
            <li><strong>Právo podat stížnost</strong> — u Úřadu pro ochranu osobních údajů (ÚOOÚ), Pplk. Sochora 27, 170 00 Praha 7, <a href="https://www.uoou.cz" target="_blank" rel="noopener">www.uoou.cz</a>.</li>
          </ul>
          <p>
            Pro uplatnění práv nás kontaktujte na e-mailu: <strong>[email]</strong>.<br />
            Na žádost odpovíme nejpozději do 30 dnů.
          </p>
        </section>

        <section class="pp-section">
          <h2>8. Zabezpečení</h2>
          <p>
            Hesla jsou ukládána výhradně v hashované podobě (bcrypt). Komunikace
            probíhá přes zabezpečené HTTPS spojení. Přístup k datům je omezen na
            minimální počet oprávněných osob.
          </p>
        </section>

        <section class="pp-section">
          <h2>9. Změny zásad</h2>
          <p>
            Vyhrazujeme si právo tyto zásady kdykoli aktualizovat.
            O podstatných změnách budeme uživatele informovat prostřednictvím
            notifikace v aplikaci. Datum poslední aktualizace je uveden v záhlaví
            tohoto dokumentu.
          </p>
        </section>
      </template>

      <!-- EN -->
      <template v-else>
        <h1>Privacy Policy · ChallengeMe</h1>
        <p class="updated">Last updated: April 2026</p>

        <section class="pp-section">
          <h2>1. Data controller</h2>
          <p>
            The data controller within the meaning of Art. 4(7) GDPR is the operator
            of the ChallengeMe platform. Contact email: <strong>[email]</strong>.
          </p>
        </section>

        <section class="pp-section">
          <h2>2. Personal data we process</h2>
          <table class="pp-table">
            <thead>
              <tr><th>Category</th><th>Specific data</th><th>Purpose</th></tr>
            </thead>
            <tbody>
              <tr>
                <td>Account</td>
                <td>Username, email address, year of birth, hashed password</td>
                <td>Account registration and management</td>
              </tr>
              <tr>
                <td>Activity</td>
                <td>Game statistics (wins, losses, draws, rating), challenges, results</td>
                <td>Leaderboard and challenge features</td>
              </tr>
              <tr>
                <td>Location</td>
                <td>GPS coordinates of a challenge (entered when creating a challenge)</td>
                <td>Displaying challenges on the map</td>
              </tr>
              <tr>
                <td>Communication</td>
                <td>Content of messages sent via the platform chat</td>
                <td>Communication between users</td>
              </tr>
              <tr>
                <td>Safety</td>
                <td>User report records</td>
                <td>Moderation and platform safety</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="pp-section">
          <h2>3. Legal basis for processing</h2>
          <ul>
            <li><strong>Performance of a contract (Art. 6(1)(b) GDPR)</strong> — processing necessary to operate your account and platform features (challenges, chat, leaderboard).</li>
            <li><strong>Legitimate interests (Art. 6(1)(f) GDPR)</strong> — ensuring platform security, preventing abuse and moderating content.</li>
          </ul>
        </section>

        <section class="pp-section">
          <h2>4. Cookies</h2>
          <p>We use two types of cookies:</p>
          <table class="pp-table">
            <thead>
              <tr><th>Type</th><th>Example</th><th>Purpose</th><th>Consent</th></tr>
            </thead>
            <tbody>
              <tr>
                <td><strong>Essential</strong></td>
                <td>Session cookie (HttpOnly)</td>
                <td>Login and security</td>
                <td>Not required</td>
              </tr>
              <tr>
                <td><strong>Analytics</strong></td>
                <td>Google Analytics (_ga, _gid)</td>
                <td>Anonymous usage statistics</td>
                <td>Required — granted via the banner on first visit</td>
              </tr>
            </tbody>
          </table>
          <p>
            Analytics cookies are activated <strong>only if you consent</strong>
            via the banner shown on your first visit. You can withdraw consent at any
            time by clearing localStorage in your browser settings or by clicking
            "Cookie settings" in the page footer.
          </p>
          <p>
            Google Analytics processes data in anonymised form (IP anonymisation enabled).
            More information: <a href="https://policies.google.com/privacy" target="_blank" rel="noopener">Google Privacy Policy</a>.
          </p>
        </section>

        <section class="pp-section">
          <h2>5. Recipients of personal data</h2>
          <p>
            We <strong>do not sell or share</strong> personal data with third parties
            for marketing purposes. Data is stored on the operator's servers and is not
            transferred outside the European Economic Area.
          </p>
          <p>
            Your username and game statistics are visible to other logged-in users
            within the platform (leaderboard, player profile).
          </p>
        </section>

        <section class="pp-section">
          <h2>6. Retention periods</h2>
          <ul>
            <li><strong>Account data</strong> — for the duration of the account. Irreversibly deleted upon account deletion.</li>
            <li><strong>Game records</strong> — for the duration of the account.</li>
            <li><strong>Chat messages</strong> — for the duration of the account or until the conversation is deleted.</li>
            <li><strong>Report records</strong> — retained for safety purposes for as long as necessary.</li>
          </ul>
        </section>

        <section class="pp-section">
          <h2>7. Your rights (GDPR)</h2>
          <ul>
            <li><strong>Right of access (Art. 15)</strong> — you may request an overview of data we process about you.</li>
            <li><strong>Right to rectification (Art. 16)</strong> — incorrect data can be corrected in your profile settings.</li>
            <li><strong>Right to erasure (Art. 17)</strong> — delete your account and all associated data via <em>Profile → Delete Account</em>.</li>
            <li><strong>Right to data portability (Art. 20)</strong> — upon request we will provide your data in a machine-readable format.</li>
            <li><strong>Right to object (Art. 21)</strong> — to processing based on legitimate interests.</li>
            <li><strong>Right to lodge a complaint</strong> — with the Czech Data Protection Authority (ÚOOÚ), Pplk. Sochora 27, 170 00 Prague 7, <a href="https://www.uoou.cz" target="_blank" rel="noopener">www.uoou.cz</a>.</li>
          </ul>
          <p>
            To exercise your rights, contact us at: <strong>[email]</strong>.<br />
            We will respond within 30 days.
          </p>
        </section>

        <section class="pp-section">
          <h2>8. Security</h2>
          <p>
            Passwords are stored exclusively in hashed form (bcrypt). All communication
            takes place over encrypted HTTPS connections. Access to data is restricted
            to the minimum number of authorised persons.
          </p>
        </section>

        <section class="pp-section">
          <h2>9. Changes to this policy</h2>
          <p>
            We reserve the right to update this policy at any time.
            Users will be notified of material changes via an in-app notification.
            The date of the last update is shown at the top of this document.
          </p>
        </section>
      </template>

    </div>
  </div>
</template>

<script>
export default {
  name: 'PrivacyPage',
}
</script>

<style scoped>
.pp-page {
  background: var(--bg-base);
  min-height: 100vh;
  padding: 40px 20px 80px;
}

.pp-wrap {
  max-width: 760px;
  margin: 0 auto;
}

h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -.02em;
  margin-bottom: 6px;
}

.updated {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 36px;
}

.pp-section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border-muted);
}
.pp-section:last-child { border-bottom: none; }

.pp-section h2 {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: .06em;
  margin-bottom: 14px;
}

.pp-section p {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.7;
  margin-bottom: 10px;
}
.pp-section p:last-child { margin-bottom: 0; }

.pp-section ul {
  padding-left: 20px;
  margin: 10px 0 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pp-section ul li {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.pp-section a {
  color: var(--brand);
  text-decoration: none;
}
.pp-section a:hover { text-decoration: underline; }

/* Data table */
.pp-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  color: var(--text-primary);
  margin-top: 10px;
}
.pp-table th {
  text-align: left;
  padding: 8px 12px;
  background: var(--bg-elevated);
  font-weight: 600;
  color: var(--text-secondary);
  border: 1px solid var(--border);
}
.pp-table td {
  padding: 9px 12px;
  border: 1px solid var(--border);
  vertical-align: top;
  line-height: 1.5;
}
.pp-table tr:nth-child(even) td { background: var(--bg-surface); }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--r);
  color: var(--text-secondary);
  font-size: 13px;
  font-family: var(--font);
  padding: 6px 14px;
  cursor: pointer;
  margin-bottom: 24px;
  transition: background var(--transition), color var(--transition);
}
.back-btn:hover {
  background: var(--bg-elevated);
  color: var(--text-primary);
}
</style>
