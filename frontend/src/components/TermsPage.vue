<template>
  <div class="tos-page">
    <div class="tos-wrap">
      <button class="back-btn" @click="$router.back()">{{ $t('common.back') }}</button>

      <!-- CS -->
      <template v-if="$i18n.locale === 'cs'">
        <h1>Podmínky užívání · ChallengeMe</h1>
        <p class="updated">Poslední aktualizace: duben 2026</p>

        <section class="tos-section">
          <h2>1. Kdo jsme a jakou roli plníme</h2>
          <p>
            ChallengeMe je technická platforma, která umožňuje registrovaným uživatelům
            zveřejňovat a přijímat sportovní a volnočasové výzvy. Platforma
            <strong>nevytváří, neorganizuje, nezprostředkovává ani jinak nezajišťuje</strong>
            žádná fyzická setkání. Veškeré výzvy jsou výhradně iniciativou samotných
            uživatelů — provozovatel není organizátorem, pořadatelem ani účastníkem
            žádného setkání.
          </p>
          <p>
            Uživatel je plně odpovědný za obsah, který na platformě zveřejní, a za svá
            rozhodnutí účastnit se výzev.
          </p>
        </section>

        <section class="tos-section">
          <h2>2. Věkové omezení</h2>
          <p>
            Služba je určena výhradně osobám starším <strong>18 let</strong>.
            Registrací uživatel prohlašuje, že je plnoletý podle právního řádu země,
            ve které se nachází, a nese plnou odpovědnost za pravdivost zadaného roku
            narození. Provozovatel není povinen věk ověřovat nad rámec poskytnutých údajů.
          </p>
        </section>

        <section class="tos-section">
          <h2>3. Setkání — odpovědnost uživatele</h2>
          <p>
            Fyzická setkání iniciovaná prostřednictvím platformy probíhají
            <strong>zcela dobrovolně a na vlastní riziko</strong> zúčastněných osob.
            Provozovatel není stranou žádného setkání, neručí za chování ostatních
            uživatelů a nenese odpovědnost za jakoukoli přímou ani nepřímou újmu
            (na zdraví, majetku ani jiné), která by mohla při setkání vzniknout.
          </p>
          <p>
            Provozovatel si vyhrazuje právo kdykoli a bez předchozího upozornění
            odstranit libovolný obsah nebo zrušit/pozastavit libovolný účet, pokud
            dojde k porušení těchto podmínek nebo platných právních předpisů.
          </p>
        </section>

        <section class="tos-section safety-section">
          <h2>4. Bezpečnostní doporučení</h2>
          <p>Před každým setkáním s cizí osobou doporučujeme:</p>
          <ul>
            <li>Setkávat se vždy <strong>na veřejném místě</strong> (park, náměstí, sportovní hřiště…)</li>
            <li>Informovat někoho blízkého o místě, čase a osobě, se kterou se setkáváš</li>
            <li>Nevycházet sám/sama v pozdních nočních hodinách</li>
            <li>Důvěřovat svému úsudku — setkání lze kdykoli zrušit bez udání důvodu</li>
            <li>V případě podezřelého chování ihned kontaktovat záchrannou službu (112)</li>
          </ul>
          <p>
            Tato doporučení jsou pouze informativní a nepředstavují záruky bezpečnosti.
          </p>
        </section>

        <section class="tos-section">
          <h2>5. Zakázané chování</h2>
          <p>Uživatelé nesmí platformu využívat k:</p>
          <ul>
            <li>Nezákonnému jednání jakéhokoli druhu</li>
            <li>Obtěžování, zastrašování nebo šikaně jiných uživatelů</li>
            <li>Vytváření falešných profilů nebo zveřejňování nepravdivých informací</li>
            <li>Zveřejňování výzev s úmyslem způsobit jinému újmu</li>
            <li>Shromažďování osobních údajů jiných uživatelů bez jejich souhlasu</li>
          </ul>
          <p>
            Porušení těchto pravidel může vést k okamžitému a trvalému zrušení účtu
            bez nároku na náhradu.
          </p>
        </section>

        <section class="tos-section">
          <h2>6. Nahlašování a moderace</h2>
          <p>
            Každý uživatel může nahlásit jiného uživatele prostřednictvím tlačítka
            „Nahlásit" na stránce profilu. Provozovatel se zavazuje podané reporty
            přezkoumávat a v odůvodněných případech přijmout opatření včetně
            dočasného nebo trvalého zablokování účtu.
          </p>
          <p>
            Provozovatel však není povinen jednat okamžitě a nenese odpovědnost
            za škody způsobené jednáním nahlášeného uživatele v době před
            provedením moderátorského zásahu.
          </p>
        </section>

        <section class="tos-section">
          <h2>7. Ochrana osobních údajů (GDPR)</h2>
          <p>Podrobné informace o zpracování osobních údajů a cookies naleznete v samostatném dokumentu: <router-link to="/privacy" class="tos-link">Zásady ochrany osobních údajů</router-link>.</p>
          <p><strong>Co sbíráme:</strong></p>
          <ul>
            <li>Uživatelské jméno a e-mailová adresa (pro správu účtu)</li>
            <li>Rok narození (pro ověření věkové způsobilosti)</li>
            <li>Lokace výzev — GPS souřadnice, pokud je uživatel zadá při vytváření výzvy</li>
            <li>Obsah zpráv zaslaných v rámci chatu platformy</li>
            <li>Herní statistiky (výhry, prohry, remízy)</li>
          </ul>
          <p>
            <strong>Proč:</strong> Data jsou využívána výhradně k provozování
            platformy a jejích funkcí. Neprodáváme ani nesdílíme osobní údaje
            třetím stranám za účelem marketingu.
          </p>
          <p>
            <strong>Právo na výmaz (GDPR čl. 17):</strong> Svůj účet a všechna
            přidružená data můžeš kdykoli trvale smazat v sekci
            <em>Profil → Smazat účet</em>. Po smazání jsou data nenávratně odstraněna.
          </p>
          <p>
            <strong>Správce osobních údajů:</strong> Provozovatel platformy ChallengeMe.
            V případě dotazů k ochraně osobních údajů nás kontaktujte na adrese
            uvedené v sekci 9.
          </p>
        </section>

        <section class="tos-section disclaimer-section">
          <h2>8. Omezení odpovědnosti</h2>
          <p>
            V maximálním rozsahu povoleném platným právem provozovatel
            <strong>nepřijímá odpovědnost</strong> za jakoukoli přímou, nepřímou,
            náhodnou ani následnou újmu vzniklou:
          </p>
          <ul>
            <li>Použitím nebo nemožností použití platformy</li>
            <li>Fyzickými setkáními organizovanými uživateli prostřednictvím platformy</li>
            <li>Chováním jiných uživatelů</li>
            <li>Výpadky, chybami nebo bezpečnostními incidenty na straně infrastruktury</li>
          </ul>
          <p>
            Platforma je poskytována „tak, jak je" (as-is) bez jakýchkoli záruk
            dostupnosti nebo bezchybného provozu.
          </p>
        </section>

        <section class="tos-section">
          <h2>9. Kontakt</h2>
          <p>
            Máš-li dotazy k těmto podmínkám, ochraně osobních údajů nebo hlášení
            závažných bezpečnostních problémů, kontaktuj nás přes formulář hlášení
            v aplikaci nebo e-mailem na adresu uvedenou v popisu aplikace.
          </p>
          <p class="tos-footer-note">
            Používáním platformy potvrzuješ, že jsi tyto podmínky přečetl/a,
            pochopil/a a souhlasíš s nimi.
          </p>
        </section>
      </template>

      <!-- EN -->
      <template v-else>
        <h1>Terms of Service · ChallengeMe</h1>
        <p class="updated">Last updated: April 2026</p>

        <section class="tos-section">
          <h2>1. Who we are and our role</h2>
          <p>
            ChallengeMe is a technical platform that enables registered users to post
            and accept sporting and leisure challenges. The platform does
            <strong>not create, organise, facilitate or otherwise arrange</strong>
            any physical meetings. All challenges are solely initiated by users
            themselves — the operator is not an organiser, host or participant
            of any meeting.
          </p>
          <p>
            The user is fully responsible for any content they publish on the platform
            and for their own decisions to participate in challenges.
          </p>
        </section>

        <section class="tos-section">
          <h2>2. Age requirement</h2>
          <p>
            The service is intended exclusively for persons aged <strong>18 or over</strong>.
            By registering, the user declares that they are of legal age under the law of
            the country in which they are located, and bears full responsibility for the
            accuracy of the birth year provided. The operator is not obliged to verify
            age beyond the information supplied.
          </p>
        </section>

        <section class="tos-section">
          <h2>3. Meetings — user responsibility</h2>
          <p>
            Physical meetings initiated through the platform take place
            <strong>entirely voluntarily and at the participants' own risk</strong>.
            The operator is not a party to any meeting, does not guarantee the conduct
            of other users, and accepts no liability for any direct or indirect harm
            (to health, property or otherwise) that may arise from a meeting.
          </p>
          <p>
            The operator reserves the right to remove any content or suspend/terminate
            any account at any time and without prior notice if these terms or
            applicable laws are violated.
          </p>
        </section>

        <section class="tos-section safety-section">
          <h2>4. Safety recommendations</h2>
          <p>Before meeting a stranger we recommend:</p>
          <ul>
            <li>Always meet in a <strong>public place</strong> (park, square, sports ground…)</li>
            <li>Tell someone you trust where you are going, who you are meeting and when</li>
            <li>Avoid going out alone late at night</li>
            <li>Trust your instincts — you can cancel a meeting at any time without reason</li>
            <li>If you feel unsafe, contact emergency services immediately (112)</li>
          </ul>
          <p>
            These recommendations are informational only and do not constitute
            guarantees of safety.
          </p>
        </section>

        <section class="tos-section">
          <h2>5. Prohibited conduct</h2>
          <p>Users must not use the platform to:</p>
          <ul>
            <li>Engage in any unlawful activity</li>
            <li>Harass, intimidate or bully other users</li>
            <li>Create fake profiles or publish false information</li>
            <li>Post challenges with the intent to harm another person</li>
            <li>Collect personal data of other users without their consent</li>
          </ul>
          <p>
            Violations may result in immediate and permanent account termination
            without compensation.
          </p>
        </section>

        <section class="tos-section">
          <h2>6. Reporting and moderation</h2>
          <p>
            Any user can report another user via the "Report" button on their profile page.
            The operator undertakes to review submitted reports and, where justified,
            take action including temporary or permanent account suspension.
          </p>
          <p>
            The operator is not obliged to act immediately and accepts no liability
            for harm caused by a reported user prior to any moderation action being taken.
          </p>
        </section>

        <section class="tos-section">
          <h2>7. Privacy &amp; Data Protection (GDPR)</h2>
          <p>For detailed information on personal data processing and cookies, see our separate document: <router-link to="/privacy" class="tos-link">Privacy Policy</router-link>.</p>
          <p><strong>What we collect:</strong></p>
          <ul>
            <li>Username and email address (for account management)</li>
            <li>Year of birth (to verify age eligibility)</li>
            <li>Challenge location — GPS coordinates entered by the user when creating a challenge</li>
            <li>Content of messages sent via the platform chat</li>
            <li>Game statistics (wins, losses, draws)</li>
          </ul>
          <p>
            <strong>Why:</strong> Data is used solely to operate the platform and its
            features. We do not sell or share personal data with third parties for
            marketing purposes.
          </p>
          <p>
            <strong>Right to erasure (GDPR Art. 17):</strong> You can permanently delete
            your account and all associated data at any time via
            <em>Profile → Delete Account</em>. Once deleted, data is irreversibly removed.
          </p>
          <p>
            <strong>Data controller:</strong> Operator of the ChallengeMe platform.
            For privacy-related enquiries, contact us at the address provided in section 9.
          </p>
        </section>

        <section class="tos-section disclaimer-section">
          <h2>8. Limitation of liability</h2>
          <p>
            To the maximum extent permitted by applicable law, the operator
            <strong>accepts no liability</strong> for any direct, indirect, incidental
            or consequential harm arising from:
          </p>
          <ul>
            <li>Use of or inability to use the platform</li>
            <li>Physical meetings organised by users through the platform</li>
            <li>The conduct of other users</li>
            <li>Outages, errors or security incidents on the infrastructure side</li>
          </ul>
          <p>
            The platform is provided "as is" without any warranties of availability
            or error-free operation.
          </p>
        </section>

        <section class="tos-section">
          <h2>9. Contact</h2>
          <p>
            If you have questions about these terms, data protection, or wish to
            report a serious security issue, contact us via the report form in the
            app or by email at the address listed in the app description.
          </p>
          <p class="tos-footer-note">
            By using the platform you confirm that you have read, understood and
            agree to these Terms of Service.
          </p>
        </section>
      </template>

    </div>
  </div>
</template>

<script>
export default {
  name: 'TermsPage',
}
</script>

<style scoped>
.tos-page {
  background: var(--bg-base);
  min-height: 100vh;
  padding: 40px 20px 80px;
}

.tos-wrap {
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

.tos-section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border-muted);
}
.tos-section:last-child {
  border-bottom: none;
}

.tos-section h2 {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: .06em;
  margin-bottom: 14px;
}

.tos-section p {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.7;
  margin-bottom: 10px;
}
.tos-section p:last-child { margin-bottom: 0; }

.tos-section ul {
  padding-left: 20px;
  margin: 10px 0 12px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.tos-section ul li {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

/* Safety section highlighted */
.safety-section {
  background: var(--yellow-muted);
  border: 1px solid rgba(210,153,34,.25);
  border-left: 3px solid var(--yellow);
  border-radius: var(--r-md);
  padding: 20px 22px;
}
.safety-section h2 { color: var(--yellow); }
.safety-section p,
.safety-section li { color: var(--text-primary); }

/* Disclaimer box */
.disclaimer-section {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  padding: 20px 22px;
}

.tos-link { color: var(--brand); text-decoration: none; }
.tos-link:hover { text-decoration: underline; }

.tos-footer-note {
  font-size: 12px;
  color: var(--text-muted);
  font-style: italic;
  margin-top: 12px;
}

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
