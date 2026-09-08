package com.example.service

import com.example.data.model.GeneratedIdentity
import org.json.JSONObject

object AutomationScriptBuilder {

    fun buildAntiDetectionScript(): String {
        return """
(function() {
  try {
    var getParameter = WebGLRenderingContext.prototype.getParameter;
    WebGLRenderingContext.prototype.getParameter = function(parameter) {
      if (parameter === 37445) return 'Intel Inc.';
      if (parameter === 37446) return 'Intel Iris OpenGL Engine';
      return getParameter.call(this, parameter);
    };
  } catch(e) {}

  try {
    Object.defineProperty(navigator, 'webdriver', { get: () => undefined, configurable: true });
    Object.defineProperty(navigator, 'plugins', { get: () => [1,2,3,4,5], configurable: true });
    Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 4, configurable: true });
    Object.defineProperty(navigator, 'deviceMemory', { get: () => 8, configurable: true });

    var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
    HTMLCanvasElement.prototype.toDataURL = function(type) {
      if (type === 'image/png' && this.width === 16 && this.height === 16) {
        return origToDataURL.apply(this, arguments);
      }
      var result = origToDataURL.apply(this, arguments);
      return result.slice(0, -6) + btoa(String.fromCharCode(Math.floor(Math.random() * 256))) + '==';
    };
  } catch(e) {}
  console.log('[CPA] Anti-detection script active');
})();
true;
        """.trimIndent()
    }

    fun buildTimezoneScript(timezone: String, language: String): String {
        return """
(function() {
  try {
    const tz = '$timezone';
    const lang = '$language';

    const OrigIntl = window.Intl;
    const OrigDTF = Intl.DateTimeFormat;
    Intl.DateTimeFormat = function(locales, options) {
      options = options || {};
      if (!options.timeZone) options.timeZone = tz;
      return new OrigDTF(lang, options);
    };
    Intl.DateTimeFormat.prototype = OrigDTF.prototype;

    Object.defineProperty(navigator, 'language', { get: () => lang, configurable: true });
    Object.defineProperty(navigator, 'languages', { get: () => [lang, lang.split('-')[0]], configurable: true });

    if (window.RTCPeerConnection) window.RTCPeerConnection = undefined;
    if (window.webkitRTCPeerConnection) window.webkitRTCPeerConnection = undefined;
    if (window.mozRTCPeerConnection) window.mozRTCPeerConnection = undefined;

    if (navigator.mediaDevices) {
      Object.defineProperty(navigator, 'mediaDevices', {
        get: () => ({ getUserMedia: () => Promise.reject(new Error('Blocked')) }),
        configurable: true
      });
    }
    console.log('[CPA] Timezone & WebRTC patched: ' + tz + ' / ' + lang);
  } catch(e) {
    console.warn('[CPA] Patch error:', e.message);
  }
})();
true;
        """.trimIndent()
    }

    fun buildSmartFormFillScript(identity: GeneratedIdentity): String {
        val identityJson = JSONObject().apply {
            put("firstName", identity.firstName)
            put("lastName", identity.lastName)
            put("fullName", identity.fullName)
            put("email", identity.email)
            put("phone", identity.phone)
            put("address", identity.address)
            put("city", identity.city)
            put("state", identity.state)
            put("postalCode", identity.postalCode)
            put("country", identity.country)
            put("birthDate", identity.birthDate)
            put("gender", identity.gender)
            put("cardNumber", identity.cardNumber)
            put("cardExpiry", identity.cardExpiry)
            put("cardCvv", identity.cardCvv)
        }.toString()

        return """
(function() {
  window._cpaIdentity = $identityJson;
  window._cpaAnsweredQuestions = window._cpaAnsweredQuestions || {};

  function logCpa(msg) {
    console.log('[CPA Auto-Pilot] ' + msg);
  }

  function showFloatingBadge(text) {
    try {
      var badge = document.getElementById('cpa-autopilot-badge');
      if (!badge) {
        badge = document.createElement('div');
        badge.id = 'cpa-autopilot-badge';
        badge.style.position = 'fixed';
        badge.style.top = '10px';
        badge.style.right = '10px';
        badge.style.zIndex = '9999999';
        badge.style.background = '#0a101d';
        badge.style.border = '1.5px solid #00f0ff';
        badge.style.borderRadius = '6px';
        badge.style.color = '#00f0ff';
        badge.style.padding = '5px 12px';
        badge.style.fontFamily = 'system-ui, -apple-system, sans-serif';
        badge.style.fontSize = '11px';
        badge.style.fontWeight = 'bold';
        badge.style.boxShadow = '0 4px 14px rgba(0,240,255,0.3)';
        badge.style.pointerEvents = 'none';
        badge.style.maxWidth = '280px';
        badge.style.whiteSpace = 'nowrap';
        badge.style.overflow = 'hidden';
        badge.style.textOverflow = 'ellipsis';
        badge.style.transition = 'all 0.3s ease';
        document.body.appendChild(badge);
      }
      badge.textContent = text;
    } catch(e) {}
  }

  function setNativeValue(element, value) {
    if (!element || value === undefined || value === null) return;
    try {
      var lastValue = element.value;
      var prototype = element.tagName === 'INPUT' ? window.HTMLInputElement.prototype :
                      element.tagName === 'SELECT' ? window.HTMLSelectElement.prototype :
                      window.HTMLTextAreaElement.prototype;
      var setter = Object.getOwnPropertyDescriptor(prototype, 'value')?.set;
      if (setter) {
        setter.call(element, value);
      } else {
        element.value = value;
      }
      // React 16+ input tracker update
      var tracker = element._valueTracker;
      if (tracker) {
        tracker.setValue(lastValue);
      }
      element.dispatchEvent(new Event('input', { bubbles: true }));
      element.dispatchEvent(new Event('change', { bubbles: true }));
      element.dispatchEvent(new Event('blur', { bubbles: true }));
    } catch(err) {
      element.value = value;
      element.dispatchEvent(new Event('input', { bubbles: true }));
      element.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }

  function triggerClick(el) {
    if (!el) return;
    try {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    } catch(e) {}

    var opts = { bubbles: true, cancelable: true, view: window };
    el.dispatchEvent(new MouseEvent('mouseenter', opts));
    el.dispatchEvent(new MouseEvent('mouseover', opts));
    el.dispatchEvent(new MouseEvent('mousedown', opts));
    el.focus();
    el.dispatchEvent(new MouseEvent('mouseup', opts));
    el.dispatchEvent(new MouseEvent('click', opts));
    if (typeof el.click === 'function') {
      el.click();
    }
  }

  function matchField(el) {
    var raw = ((el.name || '') + ' ' + (el.id || '') + ' ' + (el.placeholder || '') + ' ' + (el.getAttribute('aria-label') || '') + ' ' + (el.className || '')).toLowerCase();
    var type = (el.type || '').toLowerCase();

    if (type === 'email' || raw.match(/email|e-mail|correo|courriel/)) return 'email';
    if (type === 'tel' || raw.match(/phone|tel|mobile|cel|movil|telephone|contact/)) return 'phone';
    if (raw.match(/first.?name|fname|given.?name|prenom|vorname/)) return 'firstName';
    if (raw.match(/last.?name|lname|surname|family.?name|nom|nachname/)) return 'lastName';
    if (raw.match(/full.?name|your.?name|nombre.?completo|nom.?complet/)) return 'fullName';
    if (raw.match(/zip|postal|postcode|plz|cap|pincode/)) return 'postalCode';
    if (raw.match(/address|addr|street|rue|strasse|calle|via|direccion/)) return 'address';
    if (raw.match(/city|ville|stadt|ciudad|citta|town/)) return 'city';
    if (raw.match(/state|province|region|estado|departamento/)) return 'state';
    if (raw.match(/country|pays|land|pais/)) return 'country';
    if (raw.match(/birth|dob|birthday|date.?of.?birth/)) return 'birthDate';
    if (raw.match(/gender|sex|sexe/)) return 'gender';
    if (raw.match(/card.?number|cardnum|cc.?num|numero.?carte/)) return 'cardNumber';
    if (raw.match(/expiry|expiration|exp.?date|mm.?yy|valid/)) return 'cardExpiry';
    if (raw.match(/cvv|cvc|cvn|security.?code/)) return 'cardCvv';
    return null;
  }

  // --- Intelligent Survey Understanding Engine ---

  function getQuestionContext(element) {
    if (!element) return '';
    var curr = element.parentElement;
    var depth = 0;
    while (curr && depth < 6) {
      // Look for question headers inside container
      var headers = curr.querySelectorAll('h1, h2, h3, h4, h5, legend, [class*="question"], [class*="prompt"], [class*="title"], [class*="survey-header"], [id*="question"]');
      for (var h = 0; h < headers.length; h++) {
        var hEl = headers[h];
        if (hEl !== element && !element.contains(hEl)) {
          var txt = (hEl.innerText || hEl.textContent || '').trim().toLowerCase();
          if (txt.length > 3) return txt;
        }
      }
      // Look for preceding sibling with text
      var prev = curr.previousElementSibling;
      if (prev) {
        var ptxt = (prev.innerText || prev.textContent || '').trim().toLowerCase();
        if (ptxt.includes('?') || ptxt.includes('how') || ptxt.includes('what') || ptxt.includes('are you') || ptxt.includes('do you') || ptxt.includes('select') || ptxt.includes('choose')) {
          return ptxt;
        }
      }
      curr = curr.parentElement;
      depth++;
    }
    return '';
  }

  function pickBestSurveyOption(questionText, optionsList, ident) {
    if (!optionsList || optionsList.length === 0) return null;
    var q = (questionText || '').toLowerCase();

    function matches(opt, regex) {
      var str = ((opt.text || '') + ' ' + (opt.value || '')).toLowerCase();
      return regex.test(str);
    }

    // 1. Age & Majority Verification (CRITICAL for qualification)
    if (q.match(/age|18|years.?old|how.?old|birth|dob/)) {
      var adultOpt = optionsList.find(function(o) { return matches(o, /\b(yes|18\+|18-24|25-34|35-44|over 18|older)\b/); });
      if (adultOpt) return adultOpt;
      var nonMinor = optionsList.find(function(o) { return !matches(o, /\b(no|under 18|<18|17|minor)\b/); });
      if (nonMinor) return nonMinor;
    }

    // 2. US / Residency / Location Qualification
    if (q.match(/us.?resident|united.?states|citizen|live in the us|country|residence/)) {
      var usOpt = optionsList.find(function(o) { return matches(o, /\b(yes|united states|usa|us)\b/); });
      if (usOpt) return usOpt;
      var nonNo = optionsList.find(function(o) { return !matches(o, /\bno\b/); });
      if (nonNo) return nonNo;
    }

    // 3. Gender / Sex Matching
    if (q.match(/gender|sex|are you male|man or woman/)) {
      var isFemale = ident && ((ident.gender && ident.gender.toLowerCase().includes('female')) || (ident.firstName && /^(mary|patricia|jennifer|linda|elizabeth|barbara|susan|jessica|sarah|karen|nancy|lisa|betty|margaret|sandra|ashley|kimberly|emily|donna|michelle|carol|amanda|melissa|deborah|stephanie|rebecca|sharon|laura|cynthia|kathleen|amy|shirley|angela|helen|anna|brenda|pamela|nicole|emma|samantha|katherine|christine|debra|rachel|catherine|carolyn|janet|ruth|maria|heather|diane|virginia|julie|joyce|victoria|olivia|kelly|christina|lauren|joan|evelyn|judith|megan|cheryl|andrea|hannah|martha|jacqueline|frances|gloria|ann|teresa|kathryn|sara|janice|jean|alice|madison|doris|abigail|julia|judy|grace|denise|amber|marilyn|beverly|danielle|theresa|sophia|marie|diana|brittany|natalie|isabella|charlotte|rose|kayla|alexis)/i.test(ident.firstName)));
      if (isFemale) {
        var femOpt = optionsList.find(function(o) { return matches(o, /\b(female|woman|f|femme)\b/); });
        if (femOpt) return femOpt;
      } else {
        var maleOpt = optionsList.find(function(o) { return matches(o, /\b(male|man|m|homme)\b/); });
        if (maleOpt) return maleOpt;
      }
      return optionsList[0];
    }

    // 4. Shopping, Online Habits, Smartphone, Device
    if (q.match(/shop|online|internet|smartphone|mobile|device|phone|buy|store|amazon|walmart/)) {
      var frequentOpt = optionsList.find(function(o) { return matches(o, /\b(yes|daily|weekly|often|frequently|regularly|always|iphone|android|yes, i do)\b/); });
      if (frequentOpt) return frequentOpt;
    }

    // 5. Employment & Occupation
    if (q.match(/employ|job|work|occupation|career/)) {
      var empOpt = optionsList.find(function(o) { return matches(o, /\b(employed|full.?time|yes|professional)\b/); });
      if (empOpt) return empOpt;
    }

    // 6. Household Income (pick middle/upper-middle tier)
    if (q.match(/income|earn|salary|household/)) {
      var midIncome = optionsList.find(function(o) { return matches(o, /(50|60|75|80|100)k?|\$50|\$75/); });
      if (midIncome) return midIncome;
      if (optionsList.length >= 3) return optionsList[Math.floor(optionsList.length / 2)];
    }

    // 7. Homeownership
    if (q.match(/own or rent|homeowner|housing/)) {
      var ownOpt = optionsList.find(function(o) { return matches(o, /\b(own|homeowner|house)\b/); });
      if (ownOpt) return ownOpt;
    }

    // 8. Co-Reg Sponsor Deals / Upsells / Paid Offers / Insurance / Credit Cards
    // In co-reg walls, to proceed without payment, click "No thanks", "Skip", "Not interested"
    if (q.match(/special offer|sponsor|deal|partner|free trial|sign up for|subscription|quote|insurance|solar|card offer/)) {
      var passOpt = optionsList.find(function(o) { return matches(o, /\b(no thanks|no, thanks|no|skip|pass|not interested|continue without|no thank you|not at this time)\b/); });
      if (passOpt) return passOpt;
    }

    // 9. Ratings / Scales (e.g. 1 to 5 or 1 to 10)
    var numOptions = optionsList.filter(function(o) { return /^\d+$/.test((o.text || '').trim()); });
    if (numOptions.length >= 4) {
      return numOptions[numOptions.length - 2] || numOptions[numOptions.length - 1];
    }

    // 10. Binary Yes / No (Qualification questions almost always require "Yes")
    var yesOpt = optionsList.find(function(o) { return matches(o, /^\s*(yes|oui|si|agree|correct|definitely|absolutely)\b/); });
    var noOpt = optionsList.find(function(o) { return matches(o, /^\s*(no|non|disagree)\b/); });
    if (yesOpt && noOpt) {
      return yesOpt;
    }

    // 11. Positive sentiment matching
    var positiveOpt = optionsList.find(function(o) { return matches(o, /\b(yes|interested|agree|claim|participate|confirm|enter|proceed)\b/); });
    if (positiveOpt) return positiveOpt;

    // 12. Skip placeholders like "Select...", "Choose one..."
    var nonPlaceholder = optionsList.find(function(o) { return !matches(o, /\b(select|choose|pick|--|none)\b/); });
    if (nonPlaceholder) return nonPlaceholder;

    return optionsList[0];
  }

  // --- Handlers for Different Survey Types ---

  function handleButtonSurveys(ident) {
    // Find all choice buttons / cards inside survey containers or option lists
    var choiceSelectors = [
      'button:not([type=submit]):not([id*="submit"]):not([class*="submit"]):not([class*="continue"])',
      '[role="button"]:not([class*="submit"]):not([class*="continue"])',
      '.survey-btn', '.quiz-option', '.answer', '.choice', '.option-card', '.btn-option',
      '[data-answer]', '[data-choice]', '[data-value]', '.poll-option', '.survey-tile',
      'a.btn:not([class*="submit"]):not([class*="continue"])'
    ];

    var allButtons = Array.from(document.querySelectorAll(choiceSelectors.join(',')));
    if (allButtons.length === 0) return false;

    // Group buttons by parent container (representing a single question)
    var parentMap = new Map();
    for (var i = 0; i < allButtons.length; i++) {
      var btn = allButtons[i];
      if (btn.disabled || btn.offsetParent === null) continue;

      var parent = btn.closest('.survey-step, .question-container, .question, .quiz-step, .step, fieldset, .answers, .options, form') || btn.parentElement;
      if (!parentMap.has(parent)) {
        parentMap.set(parent, []);
      }
      parentMap.get(parent).push(btn);
    }

    var now = Date.now();
    var acted = false;

    parentMap.forEach(function(buttons, parent) {
      if (acted || buttons.length < 2) return;

      // Check if any button in this group is already selected/active
      var isAnyActive = buttons.some(function(b) {
        return b.classList.contains('active') || b.classList.contains('selected') || b.getAttribute('aria-selected') === 'true';
      });
      if (isAnyActive) return;

      var qText = getQuestionContext(parent) || getQuestionContext(buttons[0]);
      var qKey = (qText || '') + '_' + buttons.length;

      // Anti-loop protection: don't click the exact same question group within 2.5 seconds
      if (window._cpaAnsweredQuestions[qKey] && (now - window._cpaAnsweredQuestions[qKey] < 2500)) {
        return;
      }

      var optionsList = buttons.map(function(b) {
        return {
          el: b,
          text: (b.innerText || b.textContent || b.getAttribute('aria-label') || '').trim(),
          value: b.getAttribute('data-value') || b.getAttribute('value') || ''
        };
      });

      var best = pickBestSurveyOption(qText, optionsList, ident);
      if (best && best.el) {
        window._cpaAnsweredQuestions[qKey] = now;
        var chosenText = best.text || best.value || 'Selected';
        logCpa('Intelligent Survey Button Clicked: "' + chosenText + '" for question: "' + qText.slice(0, 40) + '..."');
        showFloatingBadge('⚡ Survey: ' + chosenText.slice(0, 16));
        triggerClick(best.el);
        acted = true;
      }
    });

    return acted;
  }

  function handleRadioSurveys(ident) {
    var radios = Array.from(document.querySelectorAll('input[type=radio]'));
    var groups = {};
    for (var i = 0; i < radios.length; i++) {
      var r = radios[i];
      if (r.disabled || r.offsetParent === null) continue;
      var name = r.name || ('radio_group_' + i);
      if (!groups[name]) groups[name] = [];
      groups[name].push(r);
    }

    var answeredCount = 0;
    for (var g in groups) {
      var groupRadios = groups[g];
      var anyChecked = groupRadios.some(function(r) { return r.checked; });
      if (!anyChecked && groupRadios.length > 0) {
        var qText = getQuestionContext(groupRadios[0]);
        var optionsList = groupRadios.map(function(r) {
          var labelText = r.parentElement ? (r.parentElement.innerText || r.parentElement.textContent || '') : '';
          return {
            el: r,
            text: labelText.trim(),
            value: r.value || ''
          };
        });

        var best = pickBestSurveyOption(qText, optionsList, ident);
        if (best && best.el) {
          best.el.checked = true;
          best.el.dispatchEvent(new Event('change', { bubbles: true }));
          best.el.dispatchEvent(new Event('click', { bubbles: true }));
          logCpa('Survey Radio Selected: "' + (best.text || best.value) + '"');
          showFloatingBadge('⚡ Radio: ' + (best.text || best.value).slice(0, 16));
          answeredCount++;
        }
      }
    }
    return answeredCount;
  }

  function handleSelectSurveys(ident) {
    var selects = Array.from(document.querySelectorAll('select'));
    var selectAnswered = 0;

    for (var i = 0; i < selects.length; i++) {
      var sel = selects[i];
      if (sel.disabled || sel.offsetParent === null) continue;
      if (sel.selectedIndex > 0 && sel.value && sel.value.trim().length > 0) continue;

      var qText = getQuestionContext(sel) || sel.name || sel.id || '';
      var options = Array.from(sel.options);
      if (options.length <= 1) continue;

      var optionsList = options.map(function(opt, idx) {
        return {
          el: opt,
          index: idx,
          text: (opt.text || '').trim(),
          value: opt.value || ''
        };
      });

      var best = pickBestSurveyOption(qText, optionsList, ident);
      if (best && best.index !== undefined) {
        sel.selectedIndex = best.index;
        sel.dispatchEvent(new Event('change', { bubbles: true }));
        logCpa('Survey Dropdown Selected: "' + best.text + '"');
        showFloatingBadge('⚡ Select: ' + best.text.slice(0, 16));
        selectAnswered++;
      }
    }
    return selectAnswered;
  }

  function handleCheckboxes() {
    var checkboxes = document.querySelectorAll('input[type=checkbox]');
    var checkedCount = 0;
    for (var i = 0; i < checkboxes.length; i++) {
      var cb = checkboxes[i];
      if (cb.checked || cb.disabled || cb.offsetParent === null) continue;

      var info = ((cb.name || '') + ' ' + (cb.id || '') + ' ' + (cb.className || '') + ' ' + (cb.getAttribute('aria-label') || '')).toLowerCase();
      var parentText = (cb.parentElement ? cb.parentElement.innerText : '').toLowerCase();
      var isTermsOrRequired = cb.required || 
        info.match(/agree|terms|condition|privacy|policy|optin|subscribe|age|18|consent|accept|rule|confirm/) ||
        parentText.match(/agree|terms|condition|privacy|policy|18 years|opt-in|subscribe|rules|i accept|i agree/);

      if (isTermsOrRequired) {
        cb.checked = true;
        cb.dispatchEvent(new Event('change', { bubbles: true }));
        cb.dispatchEvent(new Event('click', { bubbles: true }));
        checkedCount++;
      }
    }
    return checkedCount;
  }

  function fillInputFields() {
    var ident = window._cpaIdentity;
    if (!ident) return 0;

    var inputs = Array.from(document.querySelectorAll('input:not([type=hidden]):not([type=submit]):not([type=button]):not([type=checkbox]):not([type=radio]), select, textarea'));
    var filled = 0;

    for (var i = 0; i < inputs.length; i++) {
      var el = inputs[i];
      if (el.value && el.value.trim().length > 0 && el.tagName !== 'SELECT') {
        continue;
      }

      var field = matchField(el);
      var value = null;

      if (field === 'firstName') value = ident.firstName;
      else if (field === 'lastName') value = ident.lastName;
      else if (field === 'fullName') value = ident.fullName;
      else if (field === 'email') value = ident.email;
      else if (field === 'phone') value = ident.phone;
      else if (field === 'address') value = ident.address;
      else if (field === 'city') value = ident.city;
      else if (field === 'state') value = ident.state;
      else if (field === 'postalCode') value = ident.postalCode;
      else if (field === 'country') value = ident.country;
      else if (field === 'birthDate') value = ident.birthDate;
      else if (field === 'cardNumber' && ident.cardNumber) value = ident.cardNumber.replace(/\s/g, '');
      else if (field === 'cardExpiry' && ident.cardExpiry) value = ident.cardExpiry;
      else if (field === 'cardCvv' && ident.cardCvv) value = ident.cardCvv;

      // Smart Fallback for single-field landing pages (e.g. "Enter your email to claim $100")
      if (!value && inputs.length === 1 && (el.type === 'text' || el.type === 'email' || !el.type)) {
        value = ident.email;
      }

      if (value && el.tagName === 'SELECT') {
        var opts = el.options;
        for (var j = 0; j < opts.length; j++) {
          var optText = opts[j].text.toLowerCase();
          var optVal = opts[j].value.toLowerCase();
          if (optText.includes(value.toLowerCase()) || optVal.includes(value.toLowerCase().slice(0, 3))) {
            el.selectedIndex = j;
            el.dispatchEvent(new Event('change', { bubbles: true }));
            filled++;
            break;
          }
        }
      } else if (value) {
        setNativeValue(el, value);
        filled++;
      }
    }
    return filled;
  }

  function findAndClickSubmit() {
    var buttons = Array.from(document.querySelectorAll('button, input[type=submit], input[type=button], a[role=button], [class*="btn"], [class*="submit"], [class*="cta"], [id*="submit"], [id*="continue"], [class*="continue"], [class*="next"]'));
    var keywords = ['continue', 'next', 'submit', 'claim', 'enter', 'get started', 'proceed', 'start', 'join', 'sign up', 'agree', 'yes', 'participate', 'finish', 'go', 'win', 'reward', 'next question', 'claim reward'];

    for (var k = 0; k < buttons.length; k++) {
      var btn = buttons[k];
      if (btn.disabled || btn.offsetParent === null) continue;

      var txt = ((btn.textContent || '') + ' ' + (btn.value || '') + ' ' + (btn.getAttribute('aria-label') || '') + ' ' + (btn.id || '') + ' ' + (btn.className || '')).toLowerCase().trim();
      if (keywords.some(function(kw) { return txt.includes(kw); })) {
        logCpa('Auto-clicking action button: "' + (btn.textContent || btn.value || '').trim() + '"');
        showFloatingBadge('⚡ Action: ' + (btn.textContent || btn.value || 'Continue').trim().slice(0, 16) + '...');
        triggerClick(btn);
        return true;
      }
    }

    // Fallback: Submit form if available
    var forms = document.querySelectorAll('form');
    if (forms.length > 0) {
      for (var f = 0; f < forms.length; f++) {
        var submitBtn = forms[f].querySelector('[type=submit], button');
        if (submitBtn) {
          triggerClick(submitBtn);
          return true;
        }
      }
    }
    return false;
  }

  // --- Main Execution Cycle ---

  window._cpaExecuteCycle = function() {
    var ident = window._cpaIdentity;
    if (!ident) return;

    // 1. Fill Text / Contact Inputs
    var filled = fillInputFields();

    // 2. Check Terms / Consent Checkboxes
    var checked = handleCheckboxes();

    // 3. Handle Radio Button Surveys
    var radios = handleRadioSurveys(ident);

    // 4. Handle Dropdown Surveys
    var selects = handleSelectSurveys(ident);

    // 5. Handle Button / Card / Tile Surveys
    var buttonSurveyHandled = handleButtonSurveys(ident);

    if (filled > 0 || checked > 0 || radios > 0 || selects > 0) {
      logCpa('Cycle update: filled ' + filled + ', checked ' + checked + ', radios ' + radios + ', selects ' + selects);
      // Brief human delay before clicking Next/Submit
      setTimeout(function() {
        findAndClickSubmit();
      }, 700);
    } else if (!buttonSurveyHandled) {
      // Check if all visible fields are filled and a submit button is ready
      var visibleInputs = Array.from(document.querySelectorAll('input:not([type=hidden]):not([type=submit]):not([type=button]):not([type=checkbox]):not([type=radio])'));
      var allFilled = visibleInputs.length > 0 && visibleInputs.every(function(i) { return i.value && i.value.trim().length > 0; });
      if (allFilled) {
        findAndClickSubmit();
      }
    }
  };

  // Run cycle immediately
  window._cpaExecuteCycle();

  // Install continuous observer and interval if not already running
  if (!window._cpaObserverInstalled) {
    window._cpaObserverInstalled = true;
    showFloatingBadge('⚡ Smart Auto-Pilot: Active');

    setInterval(function() {
      if (typeof window._cpaExecuteCycle === 'function') {
        window._cpaExecuteCycle();
      }
    }, 1100);

    var obs = new MutationObserver(function() {
      if (typeof window._cpaExecuteCycle === 'function') {
        window._cpaExecuteCycle();
      }
    });

    if (document.body) {
      obs.observe(document.body, { childList: true, subtree: true });
    }
  }
})();
true;
        """.trimIndent()
    }

    fun buildHumanBehaviorScript(): String {
        return """
(function() {
  function randomScroll() {
    var scrollY = Math.random() * 180 - 90;
    window.scrollBy({ top: scrollY, behavior: 'smooth' });
    setTimeout(randomScroll, 2500 + Math.random() * 4000);
  }
  setTimeout(randomScroll, 1500);
  console.log('[CPA] Human scroll behavior running');
})();
true;
        """.trimIndent()
    }

    fun buildCompletionDetectorScript(keywords: List<String>): String {
        val kwArray = keywords.joinToString(",") { "'${it.trim().lowercase().replace("'", "\\'")}'" }
        return """
(function() {
  var keywords = [$kwArray];
  var completionReported = false;

  function checkCompletion() {
    if (completionReported) return;
    var bodyText = (document.body ? document.body.innerText : '').toLowerCase();
    var title = document.title.toLowerCase();

    for (var i = 0; i < keywords.length; i++) {
      var kw = keywords[i];
      if (kw && (bodyText.includes(kw) || title.includes(kw))) {
        completionReported = true;
        console.log('[CPA] Completion keyword detected: ' + kw);
        if (window.AndroidBridge && window.AndroidBridge.onTaskCompleted) {
          window.AndroidBridge.onTaskCompleted(kw, window.location.href);
        }
        break;
      }
    }
  }

  setTimeout(checkCompletion, 1500);
  window.addEventListener('load', function() { setTimeout(checkCompletion, 1000); });
  var obs = new MutationObserver(function() { setTimeout(checkCompletion, 500); });
  if (document.body) {
    obs.observe(document.body, { childList: true, subtree: true, characterData: true });
  }
})();
true;
        """.trimIndent()
    }
}
