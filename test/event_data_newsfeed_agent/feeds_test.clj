(ns event-data-newsfeed-agent.feeds-test
  (:require [clojure.test :refer :all]
            [event-data-newsfeed-agent.feeds :as feeds]
            [clojure.java.io :as io]))

(deftest choose-best-link
  (testing "choose-best-link tries to avoid feedproxy events"
    (is (= (feeds/choose-best-link "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/"
                                   "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/")
           "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/")
        "Non-proxy chosen")

    (is (= (feeds/choose-best-link "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/"
                                   "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/")
           "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/")
        "Non-proxy chosen irrespective of order")


    (is (= (feeds/choose-best-link nil
                                   "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/"
                                   nil
                                   "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/"
                                   nil)
           "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/")
        "nil values ignored")

    (is (= (feeds/choose-best-link "elephants.xml"
                                   "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/"
                                   "birds.csv"
                                   "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/"
                                   "marmosets.ppt")
           "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/")
        "nil values ignored")

    (is (= (feeds/choose-best-link "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/")
           "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/")
        "feed proxy chosen if no choice")

    (is (= (feeds/choose-best-link "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/"
                                   "elephants.xml")
           "http://feedproxy.google.com/~r/ResearchBloggingAllSpanish/~3/R4fIRzWfoWY/")
        "feed proxy chosen if there other invalid URLs")))

; Note that the input XML included CDATA containing HTML entity-escaped data!
(def expected-feedburner
  [{:id "b940cd8e884c2c69c6a8d0f77765d120663c1ef4",
    :url "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/",
    :relation-type-id "discusses",
    :occurred-at "2017-03-31T17:07:40Z",
    :observations [{:type :content-url, :sensitive true, :input-url "http://blog.scielo.org/es/2017/03/31/yo-escribi-eso-yo-no-escribi-eso-ahora-escribo-otra-cosa/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Yo escribí eso… yo no escribí eso… ahora escribo otra cosa…"}} ,
    
    {:id "d648e2ee348814de04451130682f95a045c71cad",
    :url "http://blogs.ciencia.unam.mx/paradigmaxxi/2017/03/31/las-creencias-erroneas-acompanan-nuestra-naturaleza/",
    :relation-type-id "discusses",
    :occurred-at "2017-03-31T08:04:38Z",
    :observations [{:type :content-url, :sensitive true, :input-url "http://blogs.ciencia.unam.mx/paradigmaxxi/2017/03/31/las-creencias-erroneas-acompanan-nuestra-naturaleza/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Las creencias erróneas acompañan nuestra naturaleza"}} ,
    
    {:id "5a5ca548960a147f7e99cb601fcf4ed9ed21170a",
    :url "http://pressreleases.scielo.org/es/2017/03/22/estudio-muestra-que-la-maloclusion-impacta-negativamente-la-calidad-de-vida-de-los-adolescentes/",
    :relation-type-id "discusses",
    :occurred-at "2017-03-22T14:37:57Z",
    :observations [{:type :content-url, :sensitive true, :input-url "http://pressreleases.scielo.org/es/2017/03/22/estudio-muestra-que-la-maloclusion-impacta-negativamente-la-calidad-de-vida-de-los-adolescentes/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Estudio muestra que la maloclusión impacta negativamente la calidad de vida de los adolescentes"}} ,
    
    {:id "cad93f6b4e58b8a0d754c79c733a8a03a44fc2d1",
    :url "http://blog.scielo.org/es/2017/03/14/internacionalizacion-como-indicador-de-desempeno-de-revistas-en-brasil-el-caso-de-la-psicologia/",
    :relation-type-id "discusses",
    :occurred-at "2017-03-14T13:30:39Z",
    :observations [{:type :content-url, :sensitive true, :input-url "http://blog.scielo.org/es/2017/03/14/internacionalizacion-como-indicador-de-desempeno-de-revistas-en-brasil-el-caso-de-la-psicologia/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Internacionalización como indicador de desempeño de revistas en Brasil: el caso de la psicología"}} ,
    
    {:id "9914f5050c030d42528ff7c6939d327378cb6013",
    :url "http://pressreleases.scielo.org/es/2017/03/06/investigacion-aborda-insercion-de-mini-implantes-sin-angulacion-vertical/",
    :relation-type-id "discusses",
    :occurred-at "2017-03-06T15:25:46Z",
    :observations [{:type :content-url, :sensitive true
    :input-url "http://pressreleases.scielo.org/es/2017/03/06/investigacion-aborda-insercion-de-mini-implantes-sin-angulacion-vertical/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Investigación aborda inserción de mini-implantes sin angulación vertical"}} ,
    
    {:id "ab81fb28f6c2881ad90a35635dcd380b6b0ab454",
    :url "http://blog.scielo.org/es/2017/03/03/in-memoriam-eugene-garfield-1925-2017/",
    :relation-type-id "discusses",
    :occurred-at "2017-03-03T14:18:06Z",
    :observations [{:type :content-url, :sensitive true
    :input-url "http://blog.scielo.org/es/2017/03/03/in-memoriam-eugene-garfield-1925-2017/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "In memoriam: Eugene Garfield – 1925-2017"}} ,
    
    {:id "7a21d97f07b17ef708b05c0ea77b38aacd2d9190",
    :url "http://elcomercio.pe/blog/expresiongenetica/2017/02/un-codigo-de-barras-para-monitorear-la-biodiversidad",
    :relation-type-id "discusses",
    :occurred-at "2017-02-22T10:22:18Z",
    :observations [{:type :content-url, :sensitive true
    :input-url "http://elcomercio.pe/blog/expresiongenetica/2017/02/un-codigo-de-barras-para-monitorear-la-biodiversidad"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Un código de barras para monitorear la biodiversidad"}} ,
 
    {:id "bdb4a6cc200cf31c5a8c1e1e9403e44b07b1c3e2",
    :url "http://blog.scielo.org/es/2017/02/22/scielo-preprints-en-camino/",
    :relation-type-id "discusses",
    :occurred-at "2017-02-22T07:03:44Z",
    :observations [{:type :content-url, :sensitive true
    :input-url "http://blog.scielo.org/es/2017/02/22/scielo-preprints-en-camino/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "SciELO Preprints en camino"}} ,
 
    {:id "e40da82e045dc58a272999d0eaf8cf9dee5a3171",
    :url "http://elcomercio.pe/blog/expresiongenetica/2017/02/secuencian-el-genoma-de-la-quinua",
    :relation-type-id "discusses",
    :occurred-at "2017-02-09T10:42:12Z",
    :observations [{:type :content-url, :sensitive true
    :input-url "http://elcomercio.pe/blog/expresiongenetica/2017/02/secuencian-el-genoma-de-la-quinua"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "Secuencian el genoma de la quinua"}} ,
 
    {:id "e663a3ea711899c7c3fb1014931c3c12ffecbaa0",
    :url "http://blog.scielo.org/es/2017/02/08/la-evaluacion-sobre-la-reproducibilidad-de-los-resultados-de-investigacion-trae-mas-preguntas-que-respuestas/",
    :relation-type-id "discusses",
    :occurred-at "2017-02-08T14:05:38Z",
    :observations [{:type :content-url, :sensitive true
    :input-url "http://blog.scielo.org/es/2017/02/08/la-evaluacion-sobre-la-reproducibilidad-de-los-resultados-de-investigacion-trae-mas-preguntas-que-respuestas/"}],
    :extra {:feed-url "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml"},
    :subj {:type "post-weblog",
    :title "La evaluación sobre la reproducibilidad de los resultados de investigación trae más preguntas que respuestas"}}])

(def expected-inoreader
  [{:id "9a28e55cd32ad2a71ed2a401c99fa73ddbf94823",
    :url "https://www.safetyrisk.net/banning-head-protection-is-safer/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T10:51:10Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://www.safetyrisk.net/banning-head-protection-is-safer/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Banning Head Protection is Safer"}} ,
   {:id "e845f55eae49eeaaf8b5e100cdee2de76ea076c8",
    :url "https://www.safetyrisk.net/im-not-playing-any-more/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T10:20:08Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://www.safetyrisk.net/im-not-playing-any-more/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "I’m Not Playing Any More"}} ,
   {:id "f776da857168ac30c1ad53566317d7a402471a1a",
    :url "https://www.safetyrisk.net/when-does-a-principal-engage-a-contractor/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T10:20:08Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://www.safetyrisk.net/when-does-a-principal-engage-a-contractor/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "When does a principal “engage” a contractor?"}} ,
   {:id "3f8de8b2f602dbaf331cd9bf22dd0475f5fb3c0a",
    :url "https://www.nationalelfservice.net/dentistry/oral-and-maxillofacial-surgery/third-molar-removal-effect-periodontal-health-second-mandibular-molars/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T05:14:15Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://www.nationalelfservice.net/dentistry/oral-and-maxillofacial-surgery/third-molar-removal-effect-periodontal-health-second-mandibular-molars/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Third molar removal: effect on periodontal health of second mandibular molars"}} ,
   {:id "ca79663b3d6fd9a33ef4b7bfb58db6d221666fc1",
    :url "http://barfblog.com/2017/04/21-sick-with-salmonella-australia-still-has-an-egg-problem-melbourne-fairytale-edition/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T04:30:06Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://barfblog.com/2017/04/21-sick-with-salmonella-australia-still-has-an-egg-problem-melbourne-fairytale-edition/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "21 sick with Salmonella: Australia still has an egg problem, Melbourne fairytale edition"}} ,
   {:id "3f3334954feac2283626e3af9b33a145d10f8df9",
    :url "http://barfblog.com/2017/04/show-me-the-data-butter-at-room-temperature-edition/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T04:30:06Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://barfblog.com/2017/04/show-me-the-data-butter-at-room-temperature-edition/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Show me the data: butter at room temperature edition"}} ,
   {:id "8ca96d5104d1608611c303aba6655f50c443185a",
    :url "http://barfblog.com/2017/04/blame-bad-souvlaki-gpa-data-to-catch-sydney-taxi-drivers-using-laneway-as-a-toilet/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T04:30:06Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://barfblog.com/2017/04/blame-bad-souvlaki-gpa-data-to-catch-sydney-taxi-drivers-using-laneway-as-a-toilet/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Blame ‘bad souvlaki’ GPA data to catch Sydney taxi drivers using laneway as a toilet"}} ,
   {:id "7a33e5e41fcbdaf64806ce5350a017cda0e1de93",
    :url "http://barfblog.com/2017/04/food-safety-talk-123-my-mom-was-pissed/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-05T04:30:06Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://barfblog.com/2017/04/food-safety-talk-123-my-mom-was-pissed/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Food Safety Talk 123: My mom was pissed"}} ,
   {:id "b0c9a3bc978371f642fac0a8f3497a35dd9b5f67",
    :url "https://newsblog.drexel.edu/2017/04/04/snapshot-researchers-showcase-wound-healing-patch-on-capitol-hill/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-04T20:02:13Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://newsblog.drexel.edu/2017/04/04/snapshot-researchers-showcase-wound-healing-patch-on-capitol-hill/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Snapshot: Researchers Showcase Wound Healing Patch on Capitol Hill"}} ,
   {:id "49f9967ad6a51a9981b1b0023552c39d2df3b642",
    :url "http://drvitelli.typepad.com/providentia/2017/04/chinese-police-arrest-caretaker-in-kindergarten-poisoning-scheme.html",
    :relation-type-id "discusses",
    :occurred-at "2017-04-04T12:19:43Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://drvitelli.typepad.com/providentia/2017/04/chinese-police-arrest-caretaker-in-kindergarten-poisoning-scheme.html"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Chinese Police Arrest Caretaker in Kindergarten Poisoning Scheme"}} ,
   {:id "fcd69c60c5c4248f0abe8daf243008178151a8ef",
    :url "http://barfblog.com/2017/04/heston-still-dont-know-food-safety-and-aust-viewers-thought-he-was-on-acid/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-04T01:25:26Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://barfblog.com/2017/04/heston-still-dont-know-food-safety-and-aust-viewers-thought-he-was-on-acid/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Heston still don’t know food safety, and Aust. viewers thought he was on acid"}} ,
   {:id "7ef8a16adadbada387eaa551763739dc005f892e",
    :url "http://barfblog.com/2017/04/fall-fairytale-lawsuit-filed-after-crypto-in-apple-cider-sickened-more-than-100-in-2015/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-04T01:25:26Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://barfblog.com/2017/04/fall-fairytale-lawsuit-filed-after-crypto-in-apple-cider-sickened-more-than-100-in-2015/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Fall fairytale: Lawsuit filed after crypto-in-apple-cider sickened more than 100 in 2015"}} ,
   {:id "843d9b60e0e0f8d02294af541a563fde9f8036ad",
    :url "http://scienceblogs.com/thepumphandle/2017/04/03/single-largest-award-in-whistleblower-case-investigated-by-osha-a-breath-of-fresh-air/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-03T21:54:07Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://scienceblogs.com/thepumphandle/2017/04/03/single-largest-award-in-whistleblower-case-investigated-by-osha-a-breath-of-fresh-air/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Single largest award in whistleblower case investigated by OSHA: a breath of fresh air"}} ,
   {:id "8f7fa29fa83c25935eb0b9563dc1f7b5f89cbecf",
    :url "https://newsblog.drexel.edu/2017/04/03/after-the-ahca-is-there-room-for-compromise-on-health-care/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-03T18:03:36Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://newsblog.drexel.edu/2017/04/03/after-the-ahca-is-there-room-for-compromise-on-health-care/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "After the AHCA: Is There Room for Compromise on Health Care?"}} ,
   {:id "5cec6bed3257681da454cc2fd1175e493d719eff",
    :url "http://scienceblogs.com/thepumphandle/2017/04/03/playing-politics-with-womens-health/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-03T16:54:00Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://scienceblogs.com/thepumphandle/2017/04/03/playing-politics-with-womens-health/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Playing politics with women’s health"}} ,
   {:id "76797a36eef42071d39f933f4d49fc673a45bb64",
    :url "https://mrepidemiology.com/2017/04/03/a-coffee-a-donut-and-a-defibrillator/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-03T13:09:15Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://mrepidemiology.com/2017/04/03/a-coffee-a-donut-and-a-defibrillator/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "A coffee, a donut, and a defibrillator"}} ,
   {:id "5c5d8b8699815a69953607b92003d638501438ad",
    :url "http://blogs.plos.org/publichealth/2017/04/03/a-coffee-a-donut-and-a-defibrillator/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-03T12:02:49Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://blogs.plos.org/publichealth/2017/04/03/a-coffee-a-donut-and-a-defibrillator/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "A coffee, a donut, and a defibrillator"}} ,
   {:id "8192091eb582878893a5e4079bbf9edff56e4e11",
    :url "https://www.nationalelfservice.net/dentistry/periodontal-disease/chlorhexidine-mouthrinse-reduced-plaque-gingivitis-used-as-adjunct-to-oral-hygiene/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-03T05:07:02Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://www.nationalelfservice.net/dentistry/periodontal-disease/chlorhexidine-mouthrinse-reduced-plaque-gingivitis-used-as-adjunct-to-oral-hygiene/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "Chlorhexidine mouthrinse: Reduced plaque and gingivitis when used as adjunct to mechanical oral hygiene procedures"}} ,
   {:id "e7504fb88745d8980d2089822c3b3b749c7aba03",
    :url "https://www.safetyrisk.net/the-unintended-consequences-of-safety-regulation/",
    :relation-type-id "discusses",
    :occurred-at "2017-04-02T22:48:47Z",
    :observations [{:type :content-url, :sensitive true :input-url "https://www.safetyrisk.net/the-unintended-consequences-of-safety-regulation/"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "The Unintended Consequences of Safety Regulation"}} ,
   {:id "9ab6f2e26d3c77dd762989680d210ecbcdf0d452",
    :url "http://haicontroversies.blogspot.com/2017/04/shea2017-storify.html",
    :relation-type-id "discusses",
    :occurred-at "2017-04-02T18:16:39Z",
    :observations [{:type :content-url, :sensitive true :input-url "http://haicontroversies.blogspot.com/2017/04/shea2017-storify.html"}],
    :extra {:feed-url "http://www.inoreader.com/stream/user/1005830516/tag/Ecology"},
    :subj {:type "post-weblog",
           :title "SHEA2017 Storify"}}]

  )

(deftest parse-feedburner
  (testing "Actions can be correcly derived from feedburner RSS feed."
    (let [result (feeds/actions-from-xml-reader "http://feeds.feedburner.com/ResearchBloggingAllSpanish?format=xml" (io/reader "resources/test/researchblogging-feedburner.xml"))]
      (is (= result expected-feedburner)))))

(deftest parse-innoreader
  (testing "Actions can be correctly derived from innoreader RSS feed."
    (let [result (feeds/actions-from-xml-reader "http://www.inoreader.com/stream/user/1005830516/tag/Ecology" (io/reader "resources/test/publichealth-inoreader.xml"))]
      (is (= result expected-inoreader)))))

