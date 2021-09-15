var langCookieName = "langSelect"

function selectLang(lang, currentLang, destination){
    $.cookie(langCookieName, lang, {expires : 100})
    if (lang === currentLang){
        return;
    }
    window.location=destination;
}