$(function(){
    $('code[data-language]').each(function () {
        var $this = $(this);
        var lang = $this.attr('data-language');
        var grammar = Prism.languages[lang];
        if (grammar) {
            var text = $this.text();
            var html = Prism.highlight(text, grammar, lang);
            $this.html(html);
        }
    })
});