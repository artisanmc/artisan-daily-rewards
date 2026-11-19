import { createModuleI18n as ae, useProject as K, useItemTexture as ie, McText as I, ItemValueInput as se, ActionChainEditor as le, useContentLang as re, textValue as W, projectLangs as oe, injectModuleStyles as de } from "@artisan/editor-sdk";
import { jsxs as d, jsx as s, Fragment as J } from "react/jsx-runtime";
import { useState as ce } from "react";
const ue = 28, me = 7, pe = "with_default_day";
function F(e) {
  return e.with_default_day === !0;
}
function V(e, t) {
  const n = t[pe];
  return typeof n == "boolean" ? n : F(e);
}
function y(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? e : void 0;
}
function R(e) {
  const t = Number(e.cycle_length);
  return Number.isInteger(t) && t >= 1 ? t : ue;
}
function _(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [n, i] of Object.entries(y(e.days) ?? {})) {
    const a = Number(n);
    Number.isInteger(a) && a >= 1 && t.set(a, y(i) ?? {});
  }
  return t;
}
function fe(e) {
  const t = R(e), n = _(e), i = y(e.default_day);
  return Array.from({ length: t }, (a, l) => {
    const r = l + 1, o = n.get(r), f = o !== void 0 && i !== void 0 && V(e, o);
    return { day: r, own: o !== void 0, reward: o ?? i, stacks: f };
  });
}
function ye(e) {
  const t = R(e);
  return [..._(e).keys()].filter((n) => n > t).sort((n, i) => n - i);
}
function he(e, t, n) {
  const i = Object.entries(y(e.days) ?? {}).filter(([a]) => Number(a) !== t);
  return n !== void 0 && i.push([String(t), n]), i.sort(([a], [l]) => Number(a) - Number(l)), Object.fromEntries(i);
}
function ge(e) {
  return e === void 0 ? e : JSON.parse(JSON.stringify(e));
}
function be(e) {
  if (typeof e == "string") return e;
  const t = y(e), n = t?.extends ?? t?.item ?? t?.material;
  return typeof n == "string" ? n : "";
}
function ve(e) {
  const t = y(e)?.count;
  return typeof t == "number" && t > 1 ? t : 1;
}
function xe(e) {
  return Array.isArray(e?.actions) ? e.actions.length : 0;
}
function we(e, t, n) {
  const i = y(e)?.title;
  if (typeof i == "string") return i;
  const a = y(i);
  if (!a) return "";
  if (typeof a.key == "string") return n(a.key);
  const l = a[t];
  if (typeof l == "string") return l;
  const r = Object.values(a).find((o) => typeof o == "string");
  return typeof r == "string" ? r : "";
}
function Ne(e, t) {
  const n = ge(e), i = y(n?.icon);
  if (!i) return n;
  for (const a of ["title", "lore"]) {
    const l = y(i[a]);
    l && typeof l.key == "string" && (i[a] = t(l.key));
  }
  return n;
}
function L(e) {
  const t = Number(e);
  return Number.isInteger(t) && t >= 1 ? t : 0;
}
function G(e) {
  const t = e.milestones;
  return Array.isArray(t) ? t.map((n) => y(n) ?? {}) : [];
}
function U(e) {
  const t = L(e.at), n = L(e.every);
  if (t > 0) return { at: t, every: 0, from: 0, icon: e.icon, actions: e.actions };
  if (n > 0) return { at: 0, every: n, from: L(e.from) || n, icon: e.icon, actions: e.actions };
}
function S(e) {
  return G(e).map(U).filter((t) => t !== void 0);
}
function ke(e, t) {
  return t < 1 ? !1 : e.every === 0 ? t === e.at : t >= e.from && (t - e.from) % e.every === 0;
}
function $(e, t) {
  const n = Math.max(t, 1);
  return e.every === 0 ? e.at >= n ? e.at : -1 : n <= e.from ? e.from : e.from + Math.ceil((n - e.from) / e.every) * e.every;
}
function Ae(e, t) {
  return e.filter((n) => ke(n, t));
}
function O(e, t, n) {
  const i = [], a = e.map((l) => $(l, t));
  for (; i.length < n; ) {
    const l = a.filter((o) => o > 0);
    if (l.length === 0) break;
    const r = Math.min(...l);
    for (let o = 0; o < a.length && i.length < n; o++) {
      if (a[o] !== r) continue;
      const f = e[o];
      i.push({ streak: r, milestone: f }), a[o] = f.every > 0 ? $(f, r + 1) : -1;
    }
  }
  return i;
}
function _e(e) {
  return e.length > 0 && !e.some((t) => t.every > 0);
}
function X(e, t) {
  return e.every === 0 ? t("milestoneAt", { at: e.at }) : e.from === e.every ? t("milestoneEvery", { every: e.every }) : t("milestoneEveryFrom", { every: e.every, from: e.from });
}
const Se = "daily:calendar", q = "daily:status", Y = "daily:milestones", Te = 28, Ee = 200, k = 5, De = "rewards", Re = 7, je = 20, B = 35, E = { material: "PAPER" }, Q = 864e5;
function Z(e) {
  const t = e?.dataSources?.find(
    (n) => String(n.id) === De
  );
  if (!(!t || t.shape !== "config"))
    return y(t.value);
}
function Ce(e) {
  if (Z(e)) return !0;
  const t = e;
  return t?.ownerModule === "daily" || String(t?.id ?? "") === "daily" || String(t?.id ?? "").endsWith(":daily");
}
function A(e, t) {
  return e < 1 ? 1 : (e - 1) % t + 1;
}
function ee(e, t) {
  if (!(t < 1))
    return _(e).get(t) ?? y(e.default_day);
}
function T(e, t, n) {
  const i = e?.[t];
  if (i === void 0) return n;
  const a = Number.parseInt(String(i).trim(), 10);
  return Number.isNaN(a) ? n : a;
}
function He(e, t) {
  return t < 2 ? 0 : (e - 1) % t;
}
function Le(e, t, n) {
  const i = [
    { status: "claimed", streak: 1 },
    { status: "claimed", streak: 2 },
    { status: "claimed", streak: 3 },
    { status: "missed", streak: 0 },
    { status: "available", streak: 4 }
  ], a = [];
  for (let r = -e; r < 0; r++) {
    const o = i[i.length + r];
    a.push(
      o ? {
        offset: r,
        status: o.status,
        cycleDay: o.streak > 0 ? A(o.streak, n) : 0,
        streak: o.streak
      } : { offset: r, status: "before", cycleDay: 0, streak: 0 }
    );
  }
  const l = k;
  a.push({ offset: 0, status: "available", cycleDay: A(l, n), streak: l });
  for (let r = 1; r <= t; r++)
    a.push({ offset: r, status: "upcoming", cycleDay: A(l + r, n), streak: l + r });
  return a;
}
function N(e) {
  return String(e).padStart(2, "0");
}
function Me(e, t, n) {
  const i = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + e.offset * Q), a = ee(t, e.cycleDay);
  return {
    id: `${i.getUTCFullYear()}-${N(i.getUTCMonth() + 1)}-${N(i.getUTCDate())}`,
    date: `${N(i.getUTCDate())}/${N(i.getUTCMonth() + 1)}`,
    offset: e.offset,
    status: e.status,
    is_today: e.offset === 0,
    is_claimed: e.status === "claimed",
    is_missed: e.status === "missed",
    is_before: e.status === "before",
    is_available: e.status === "available",
    is_upcoming: e.status === "upcoming",
    cycle_day: e.cycleDay,
    has_reward: a !== void 0,
    icon: a?.icon ?? E,
    // The sample player's streak never broke: each streak that a milestone matches pays it.
    ...te(S(t), e.streak, e.streak > 0, a?.icon ?? E)
  };
}
function te(e, t, n, i) {
  const a = n ? Ae(e, t) : [];
  return {
    streak: t,
    is_milestone: a.length > 0,
    milestone_count: a.length,
    milestone_icon: a.find((l) => l.icon != null)?.icon ?? i
  };
}
function Fe(e, t, n) {
  const i = S(e);
  return O(i, k, t).map(({ streak: a, milestone: l }) => {
    const r = a - k, o = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + r * Q);
    return {
      id: `${a}-${i.indexOf(l) + 1}`,
      streak: a,
      offset: r,
      date: `${N(o.getUTCDate())}/${N(o.getUTCMonth() + 1)}`,
      is_today: r === 0,
      repeats: l.every > 0,
      every: l.every,
      icon: l.icon ?? E
    };
  });
}
function Ie(e, t) {
  const n = ee(t, Number(e.cycle_day) || 0), i = n?.icon ?? E, a = te(S(t), Number(e.streak) || 0, e.is_milestone === !0, i);
  return { ...e, has_reward: n !== void 0, icon: i, ...a, is_milestone: e.is_milestone === !0 };
}
function Ue(e, t = {}) {
  const n = O(S(t), k, 1)[0];
  return {
    id: "status",
    loaded: !0,
    streak: 4,
    longest: 12,
    total: 31,
    freezes: 0,
    freeze_max: 3,
    claimed_today: !1,
    unclaimed: 2,
    can_claim: !0,
    at_risk: !1,
    broken: !1,
    lost: 0,
    cycle_day: A(5, e),
    cycle_length: e,
    until_reset: "3h 12m",
    next_milestone: n ? n.streak : 0,
    next_milestone_in: n ? n.streak - k : -1
  };
}
function Oe(e, t, n, i = /* @__PURE__ */ new Date()) {
  if (t !== Se && t !== q && t !== Y || !Ce(e)) return null;
  const a = Z(e) ?? {}, l = R(a), r = n?.serverRows;
  if (t === q) {
    const c = r?.filter((m) => m.loaded === !0) ?? [];
    return c.length > 0 ? c.map((m) => ({ ...m, cycle_length: l })) : [Ue(l, a)];
  }
  if (t === Y) {
    if (r && r.length > 0) return r;
    const c = Math.max(0, Math.min(Ee, T(n?.params, "count", Te)));
    return Fe(a, c, i);
  }
  if (r && r.length > 0) return r.map((c) => Ie(c, a));
  const o = Math.max(0, Math.min(B, T(n?.params, "past", Re))), f = Math.max(0, T(n?.params, "future", je)), p = Math.min(He(A(k, l), T(n?.params, "week", 0)), f, B - o);
  return Le(o + p, f - p, l).map((c) => Me(c, a, i));
}
const Pe = {
  cycleLength: "Cycle length",
  cycleHint: "Days before the rewards start again at day 1. The streak goes on.",
  days: "{{count}} days",
  legendOwn: "Its own reward",
  legendDefault: "Reward of the other days",
  defaultTitle: "The other days",
  defaultHint: "What a day without its own reward pays.",
  defaultNone: "None: no reward on those days.",
  defaultAdd: "Add a reward for the other days",
  defaultRemove: "No reward on the other days",
  day: "Day {{day}}",
  ownHint: "Its own reward, in place of the other days' one.",
  ownStackHint: "Its own reward, paid on top of the other days' one.",
  stackLabel: "With the reward of the other days",
  stackFollow: "Default ({{mode}})",
  stackReplace: "Replaces it",
  stackAdd: "Adds to it",
  stackAllLabel: "A day with its own reward",
  stackAllHint: "Replaces this reward, or pays it too (this one first). Each day can say otherwise.",
  stackBadge: "Also pays the reward of the other days",
  legendStack: "+ also the other days' reward",
  inheritHint: "This day pays the reward of the other days.",
  customize: "Give this day its own reward",
  editDefault: "Edit the reward of the other days",
  reset: "Back to the reward of the other days",
  icon: "Icon",
  iconHint: "Shown in the calendar menu: name it after the reward.",
  actions: "Actions",
  actionsHint: "Run when the day is claimed: give_item, give_money, a command… ({streak}, {cycle_day}, {cycle_length}).",
  steps: "{{count}} step(s)",
  noActions: "no action",
  outsideTitle: "Days past the cycle",
  outsideHint: "The plugin refuses the file while these days are listed: lengthen the cycle or remove them.",
  remove: "Remove day {{day}}",
  pick: "Click a day to edit its reward.",
  milestonesTitle: "Streak milestones",
  milestonesHint: "Bonuses on top of the day's reward, when the streak reaches them. A break starts them again.",
  milestonesNone: "No milestone yet.",
  milestoneAddAt: "+ Once per streak",
  milestoneAddEvery: "+ Again and again",
  milestoneAt: "At {{at}} days",
  milestoneEvery: "Every {{every}} days",
  milestoneEveryFrom: "Every {{every}} days from {{from}}",
  milestoneInvalid: "Neither at nor every: the plugin refuses the file",
  milestoneNoRepeat: 'No milestone repeats: past the last one, a player earns more by breaking their streak than by going on. End with an "again and again" milestone.',
  milestoneKind: "Paid",
  milestoneOnce: "Once per streak",
  milestoneRepeat: "Again and again",
  milestoneAtLabel: "At streak",
  milestoneEveryLabel: "Every (days of streak)",
  milestoneFromLabel: "From streak",
  milestoneFromHint: "The first streak it pays. Empty: the same as every.",
  milestoneIconHint: "Shown in the milestones menu: name it after the bonus.",
  milestoneActionsHint: "Run with the claim of the day that reaches it ({streak}, {cycle_day}, {cycle_length}).",
  milestoneRemove: "Remove this milestone",
  milestoneNext: "Next ones from day 1: {{list}}"
}, ze = {
  cycleLength: "Longueur du cycle",
  cycleHint: "Jours avant que les récompenses reprennent au jour 1. Le streak continue.",
  days: "{{count}} jours",
  legendOwn: "Récompense propre",
  legendDefault: "Récompense des autres jours",
  defaultTitle: "Les autres jours",
  defaultHint: "Ce que rapporte un jour sans récompense propre.",
  defaultNone: "Aucune : pas de récompense ces jours-là.",
  defaultAdd: "Ajouter une récompense pour les autres jours",
  defaultRemove: "Pas de récompense les autres jours",
  day: "Jour {{day}}",
  ownHint: "Sa propre récompense, à la place de celle des autres jours.",
  ownStackHint: "Sa propre récompense, en plus de celle des autres jours.",
  stackLabel: "Avec la récompense des autres jours",
  stackFollow: "Par défaut ({{mode}})",
  stackReplace: "La remplace",
  stackAdd: "S'y ajoute",
  stackAllLabel: "Un jour avec sa propre récompense",
  stackAllHint: "Remplace cette récompense, ou la donne aussi (celle-ci d'abord). Chaque jour peut faire autrement.",
  stackBadge: "Donne aussi la récompense des autres jours",
  legendStack: "+ aussi la récompense des autres jours",
  inheritHint: "Ce jour rapporte la récompense des autres jours.",
  customize: "Donner à ce jour sa propre récompense",
  editDefault: "Modifier la récompense des autres jours",
  reset: "Revenir à la récompense des autres jours",
  icon: "Icône",
  iconHint: "Affichée dans le calendrier : nomme-la d'après la récompense.",
  actions: "Actions",
  actionsHint: "Exécutées quand le jour est réclamé : give_item, give_money, une commande… ({streak}, {cycle_day}, {cycle_length}).",
  steps: "{{count}} étape(s)",
  noActions: "aucune action",
  outsideTitle: "Jours hors du cycle",
  outsideHint: "Le plugin refuse le fichier tant que ces jours y sont : allonge le cycle ou retire-les.",
  remove: "Retirer le jour {{day}}",
  pick: "Clique sur un jour pour modifier sa récompense.",
  milestonesTitle: "Paliers de streak",
  milestonesHint: "Des bonus en plus de la récompense du jour, quand le streak les atteint. Un streak cassé les fait recommencer.",
  milestonesNone: "Aucun palier pour l'instant.",
  milestoneAddAt: "+ Une fois par streak",
  milestoneAddEvery: "+ Répétable",
  milestoneAt: "À {{at}} jours",
  milestoneEvery: "Tous les {{every}} jours",
  milestoneEveryFrom: "Tous les {{every}} jours dès {{from}}",
  milestoneInvalid: "Ni at ni every : le plugin refuse le fichier",
  milestoneNoRepeat: "Aucun palier ne se répète : passé le dernier, un joueur gagne plus à casser son streak qu'à continuer. Termine par un palier répétable.",
  milestoneKind: "Payé",
  milestoneOnce: "Une fois par streak",
  milestoneRepeat: "Répétable",
  milestoneAtLabel: "Au streak",
  milestoneEveryLabel: "Tous les (jours de streak)",
  milestoneFromLabel: "À partir du streak",
  milestoneFromHint: "Le premier streak qui le paie. Vide : le même que every.",
  milestoneIconHint: "Affichée dans le menu des paliers : nomme-la d'après le bonus.",
  milestoneActionsHint: "Exécutées avec la réclamation du jour qui l'atteint ({streak}, {cycle_day}, {cycle_length}).",
  milestoneRemove: "Supprimer ce palier",
  milestoneNext: "Les prochains depuis le jour 1 : {{list}}"
}, w = ae({ en: Pe, fr: ze });
function $e({ projectId: e, value: t, set: n, readOnly: i }) {
  const a = w(), l = y(t) ?? {}, r = fe(l), o = ye(l), f = y(l.default_day), [p, c] = ce(null), m = K(e), v = (u) => m ? W(m, u, oe(m)[0] ?? "en") : "", h = (u, g) => n(["days"], he(l, u, g)), b = G(l), x = (u) => n(["milestones"], u.length > 0 ? u : void 0), C = (u) => {
    x([...b, u]), c({ milestone: b.length });
  }, H = typeof p == "object" && p !== null ? p.milestone : -1;
  return /* @__PURE__ */ d("div", { className: "daily-rewards", children: [
    /* @__PURE__ */ d("div", { className: "daily-toolbar", children: [
      /* @__PURE__ */ d("label", { className: "daily-cycle", children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: a("cycleLength") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input daily-cycle-input",
            type: "number",
            min: 1,
            value: R(l),
            disabled: i,
            onChange: (u) => {
              const g = Number.parseInt(u.target.value, 10);
              Number.isInteger(g) && g >= 1 && n(["cycle_length"], g);
            }
          }
        ),
        /* @__PURE__ */ s("span", { className: "daily-faint", children: a("cycleHint") })
      ] }),
      /* @__PURE__ */ d("div", { className: "daily-legend", children: [
        /* @__PURE__ */ s("span", { className: "daily-legend-own", children: a("legendOwn") }),
        /* @__PURE__ */ s("span", { className: "daily-legend-default", children: a("legendDefault") }),
        r.some((u) => u.stacks) && /* @__PURE__ */ s("span", { className: "daily-legend-stack", children: a("legendStack") })
      ] })
    ] }),
    o.length > 0 && /* @__PURE__ */ d("div", { className: "daily-outside", role: "alert", children: [
      /* @__PURE__ */ s("strong", { children: a("outsideTitle") }),
      /* @__PURE__ */ s("span", { children: a("outsideHint") }),
      /* @__PURE__ */ s("div", { className: "daily-outside-days", children: o.map((u) => /* @__PURE__ */ d(
        "button",
        {
          type: "button",
          className: "chip",
          disabled: i,
          onClick: () => h(u, void 0),
          children: [
            a("remove", { day: u }),
            " ×"
          ]
        },
        u
      )) })
    ] }),
    /* @__PURE__ */ d("div", { className: "daily-layout", children: [
      /* @__PURE__ */ d("div", { className: "daily-board", children: [
        /* @__PURE__ */ d(
          "button",
          {
            type: "button",
            className: `daily-default${p === "default" ? " is-selected" : ""}`,
            onClick: () => c("default"),
            children: [
              /* @__PURE__ */ s(j, { reward: f }),
              /* @__PURE__ */ d("span", { className: "daily-default-text", children: [
                /* @__PURE__ */ s("span", { className: "daily-default-title", children: a("defaultTitle") }),
                /* @__PURE__ */ s("span", { className: "daily-faint", children: a(f ? "defaultHint" : "defaultNone") })
              ] })
            ]
          }
        ),
        /* @__PURE__ */ s("div", { className: "daily-grid", style: { gridTemplateColumns: `repeat(${me}, minmax(0, 1fr))` }, children: r.map((u) => /* @__PURE__ */ s(
          Ye,
          {
            cell: u,
            projectId: e,
            selected: p === u.day,
            onSelect: () => c(u.day)
          },
          u.day
        )) }),
        /* @__PURE__ */ s(
          Ke,
          {
            rewards: l,
            entries: b,
            projectId: e,
            selected: H,
            readOnly: i,
            onSelect: (u) => c({ milestone: u }),
            onAdd: C
          }
        )
      ] }),
      /* @__PURE__ */ s("aside", { className: "daily-panel", children: p === null ? /* @__PURE__ */ s("p", { className: "daily-faint", children: a("pick") }) : typeof p == "object" ? b[p.milestone] ? /* @__PURE__ */ s(
        Je,
        {
          projectId: e,
          entry: b[p.milestone],
          readOnly: i,
          onChange: (u) => x(b.map((g, ne) => ne === p.milestone ? u : g)),
          onRemove: () => {
            x(b.filter((u, g) => g !== p.milestone)), c(null);
          }
        }
      ) : /* @__PURE__ */ s("p", { className: "daily-faint", children: a("pick") }) : p === "default" ? /* @__PURE__ */ s(D, { title: a("defaultTitle"), hint: a(f ? "defaultHint" : "defaultNone"), children: f ? /* @__PURE__ */ d(J, { children: [
        /* @__PURE__ */ s(
          z,
          {
            projectId: e,
            reward: f,
            readOnly: i,
            onChange: (u, g) => n(["default_day", ...u], g)
          }
        ),
        /* @__PURE__ */ s("div", { className: "daily-fields", children: /* @__PURE__ */ d("div", { children: [
          /* @__PURE__ */ s("div", { className: "field-label", children: a("stackAllLabel") }),
          /* @__PURE__ */ s("div", { className: "daily-faint", children: a("stackAllHint") }),
          /* @__PURE__ */ s("div", { className: "daily-milestone-kind", children: [!1, !0].map((u) => /* @__PURE__ */ s(
            "button",
            {
              type: "button",
              className: `chip${F(l) === u ? " is-active" : ""}`,
              disabled: i,
              onClick: () => n(["with_default_day"], u),
              children: a(u ? "stackAdd" : "stackReplace")
            },
            String(u)
          )) })
        ] }) }),
        /* @__PURE__ */ s(
          "button",
          {
            type: "button",
            className: "btn btn-ghost btn-sm",
            disabled: i,
            onClick: () => n(["default_day"], void 0),
            children: a("defaultRemove")
          }
        )
      ] }) : /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: i,
          onClick: () => n(["default_day"], { icon: { material: "BREAD" }, actions: [] }),
          children: a("defaultAdd")
        }
      ) }) : /* @__PURE__ */ s(
        Be,
        {
          day: p,
          projectId: e,
          own: _(l).get(p),
          fallback: f,
          stacksAll: F(l),
          stacks: V(l, _(l).get(p) ?? {}),
          readOnly: i,
          onCustomize: () => h(p, Ne(f, v) ?? { icon: { material: "CHEST" }, actions: [] }),
          onReset: () => h(p, void 0),
          onChange: (u, g) => n(["days", qe(l, p), ...u], g),
          onEditDefault: () => c("default")
        }
      ) })
    ] })
  ] });
}
function qe(e, t) {
  return Object.keys(y(e.days) ?? {}).find((n) => Number(n) === t) ?? String(t);
}
function Ye({
  cell: e,
  projectId: t,
  selected: n,
  onSelect: i
}) {
  const a = w(), l = P(t, e.reward?.icon), r = ["daily-cell", e.own ? "is-own" : "is-default", n ? "is-selected" : ""];
  return /* @__PURE__ */ d("button", { type: "button", className: r.join(" "), onClick: i, title: a("day", { day: e.day }), children: [
    /* @__PURE__ */ s("span", { className: "daily-cell-day", children: e.day }),
    e.stacks && /* @__PURE__ */ s("span", { className: "daily-cell-stack", title: a("stackBadge"), "aria-label": a("stackBadge"), children: "+" }),
    /* @__PURE__ */ s(j, { reward: e.reward }),
    e.own && l && /* @__PURE__ */ s("span", { className: "daily-cell-title", children: /* @__PURE__ */ s(I, { source: l, maxLines: 2, align: "center", scale: 1 }) })
  ] });
}
function j({ reward: e }) {
  const t = e?.icon, n = be(t), i = ie(n), a = ve(t);
  return /* @__PURE__ */ d("span", { className: "daily-slot", children: [
    i ? /* @__PURE__ */ s("img", { src: i, alt: n, draggable: !1 }) : /* @__PURE__ */ s("span", { className: "daily-slot-name", children: n ? n.slice(0, 3) : "—" }),
    a > 1 && /* @__PURE__ */ s("span", { className: "daily-slot-count", children: a })
  ] });
}
function P(e, t) {
  const n = K(e), i = re();
  return we(t, i, (a) => n ? W(n, a, i) : "");
}
function D({ title: e, hint: t, children: n }) {
  return /* @__PURE__ */ d("div", { className: "daily-panel-body", children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: e }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: t })
    ] }),
    n
  ] });
}
function Be({
  day: e,
  projectId: t,
  own: n,
  fallback: i,
  stacksAll: a,
  stacks: l,
  readOnly: r,
  onCustomize: o,
  onReset: f,
  onChange: p,
  onEditDefault: c
}) {
  const m = w(), v = P(t, (n ?? i)?.icon);
  if (!n) {
    const h = xe(i);
    return /* @__PURE__ */ d(D, { title: m("day", { day: e }), hint: m("inheritHint"), children: [
      /* @__PURE__ */ d("div", { className: "daily-inherited", children: [
        /* @__PURE__ */ s(j, { reward: i }),
        /* @__PURE__ */ d("span", { className: "daily-inherited-text", children: [
          v ? /* @__PURE__ */ s(I, { source: v, scale: 1 }) : /* @__PURE__ */ s("span", { className: "daily-faint", children: m("defaultNone") }),
          /* @__PURE__ */ s("span", { className: "daily-faint", children: h > 0 ? m("steps", { count: h }) : m("noActions") })
        ] })
      ] }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-sm", disabled: r, onClick: o, children: m("customize") }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", onClick: c, children: m("editDefault") })
    ] });
  }
  return /* @__PURE__ */ d(D, { title: m("day", { day: e }), hint: m(i && l ? "ownStackHint" : "ownHint"), children: [
    /* @__PURE__ */ s(z, { projectId: t, reward: n, readOnly: r, onChange: p }),
    i && /* @__PURE__ */ s("fieldset", { className: "daily-fields", disabled: r, children: /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: m("stackLabel") }),
      /* @__PURE__ */ s("div", { className: "daily-milestone-kind", children: [void 0, !1, !0].map((h) => /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: `chip${n.with_default_day === h ? " is-active" : ""}`,
          onClick: () => p(["with_default_day"], h),
          children: h === void 0 ? m("stackFollow", { mode: m(a ? "stackAdd" : "stackReplace") }) : m(h ? "stackAdd" : "stackReplace")
        },
        String(h)
      )) })
    ] }) }),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: r, onClick: f, children: m("reset") })
  ] });
}
function z({
  projectId: e,
  reward: t,
  readOnly: n,
  iconHint: i,
  actionsHint: a,
  onChange: l
}) {
  const r = w();
  return /* @__PURE__ */ d("fieldset", { className: "daily-fields", disabled: n, children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: r("icon") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: i ?? r("iconHint") }),
      /* @__PURE__ */ s(se, { projectId: e, value: t.icon, onChange: (o) => l(["icon"], o) })
    ] }),
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: r("actions") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: a ?? r("actionsHint") }),
      /* @__PURE__ */ s(
        le,
        {
          projectId: e,
          value: Array.isArray(t.actions) ? t.actions : [],
          onChange: (o) => l(["actions"], o)
        }
      )
    ] })
  ] });
}
function Ke({
  rewards: e,
  entries: t,
  projectId: n,
  selected: i,
  readOnly: a,
  onSelect: l,
  onAdd: r
}) {
  const o = w(), f = S(e), p = O(f, 1, 8).map((c) => c.streak);
  return /* @__PURE__ */ d("section", { className: "daily-milestones", children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: o("milestonesTitle") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: o("milestonesHint") })
    ] }),
    _e(f) && /* @__PURE__ */ s("div", { className: "daily-outside", role: "alert", children: o("milestoneNoRepeat") }),
    t.length === 0 ? /* @__PURE__ */ s("span", { className: "daily-faint", children: o("milestonesNone") }) : /* @__PURE__ */ s("div", { className: "daily-milestone-list", children: t.map((c, m) => /* @__PURE__ */ s(
      We,
      {
        entry: c,
        projectId: n,
        selected: i === m,
        onSelect: () => l(m)
      },
      m
    )) }),
    p.length > 0 && /* @__PURE__ */ s("span", { className: "daily-faint", children: o("milestoneNext", { list: p.join(", ") }) }),
    /* @__PURE__ */ d("div", { className: "daily-milestone-add", children: [
      /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: a,
          onClick: () => r({ at: 30, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: o("milestoneAddAt")
        }
      ),
      /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: a,
          onClick: () => r({ every: 50, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: o("milestoneAddEvery")
        }
      )
    ] })
  ] });
}
function We({
  entry: e,
  projectId: t,
  selected: n,
  onSelect: i
}) {
  const a = w(), l = P(t, e.icon), r = U(e);
  return /* @__PURE__ */ d("button", { type: "button", className: ["daily-default", "daily-milestone", n ? "is-selected" : "", r ? "" : "is-invalid"].join(" "), onClick: i, children: [
    /* @__PURE__ */ s(j, { reward: e }),
    /* @__PURE__ */ d("span", { className: "daily-default-text", children: [
      /* @__PURE__ */ s("span", { className: "daily-default-title", children: r ? X(r, a) : a("milestoneInvalid") }),
      l && /* @__PURE__ */ s(I, { source: l, maxLines: 1, scale: 1 })
    ] })
  ] });
}
function M(e) {
  const t = Number.parseInt(e, 10);
  return Number.isInteger(t) && t >= 1 ? t : void 0;
}
function Je({
  projectId: e,
  entry: t,
  readOnly: n,
  onChange: i,
  onRemove: a
}) {
  const l = w(), r = U(t), o = t.at === void 0 && t.every !== void 0, f = (c, m) => {
    const v = { ...t, [c]: m };
    m === void 0 && delete v[c];
    const { at: h, every: b, from: x, ...C } = v;
    return Object.fromEntries(
      Object.entries({ at: h, every: b, from: x, ...C }).filter(([, H]) => H !== void 0)
    );
  }, p = (c) => {
    if (c === o) return;
    const { at: m, every: v, from: h, ...b } = t, x = Number(m) || Number(v) || 30;
    i(c ? { every: x, ...b } : { at: x, ...b });
  };
  return /* @__PURE__ */ d(D, { title: r ? X(r, l) : l("milestoneInvalid"), hint: l("milestonesHint"), children: [
    /* @__PURE__ */ d("fieldset", { className: "daily-fields", disabled: n, children: [
      /* @__PURE__ */ d("div", { children: [
        /* @__PURE__ */ s("div", { className: "field-label", children: l("milestoneKind") }),
        /* @__PURE__ */ d("div", { className: "daily-milestone-kind", children: [
          /* @__PURE__ */ s("button", { type: "button", className: `chip${o ? "" : " is-active"}`, onClick: () => p(!1), children: l("milestoneOnce") }),
          /* @__PURE__ */ s("button", { type: "button", className: `chip${o ? " is-active" : ""}`, onClick: () => p(!0), children: l("milestoneRepeat") })
        ] })
      ] }),
      o ? /* @__PURE__ */ d(J, { children: [
        /* @__PURE__ */ d("label", { children: [
          /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneEveryLabel") }),
          /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.every ?? ""),
              onChange: (c) => i(f("every", M(c.target.value)))
            }
          )
        ] }),
        /* @__PURE__ */ d("label", { children: [
          /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneFromLabel") }),
          /* @__PURE__ */ s("span", { className: "daily-faint", children: l("milestoneFromHint") }),
          /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.from ?? ""),
              placeholder: String(t.every ?? ""),
              onChange: (c) => i(f("from", M(c.target.value)))
            }
          )
        ] })
      ] }) : /* @__PURE__ */ d("label", { children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneAtLabel") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input",
            type: "number",
            min: 1,
            value: String(t.at ?? ""),
            onChange: (c) => i(f("at", M(c.target.value)))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ s(
      z,
      {
        projectId: e,
        reward: t,
        readOnly: n,
        iconHint: l("milestoneIconHint"),
        actionsHint: l("milestoneActionsHint"),
        onChange: (c, m) => i(f(c[0], m))
      }
    ),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: n, onClick: a, children: l("milestoneRemove") })
  ] });
}
const Ve = `
.daily-rewards { display: flex; flex-direction: column; gap: 16px; }
.daily-toolbar { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 12px; }
.daily-cycle { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.daily-cycle-input { width: 80px; }
.daily-faint { color: var(--text-faint); font-size: 12px; line-height: 1.4; }
.daily-legend { display: flex; gap: 12px; font-size: 12px; color: var(--text-mut); }
.daily-legend span { display: inline-flex; align-items: center; gap: 6px; }
.daily-legend span::before { content: ""; width: 12px; height: 12px; border-radius: 3px; }
.daily-legend-own::before { background: var(--fam-reward-soft); border: 2px solid var(--fam-reward); }
.daily-legend-default::before { background: var(--surface-inset); border: 1px solid var(--border-soft); opacity: 0.6; }
.daily-outside { display: flex; flex-direction: column; gap: 6px; padding: 10px 12px; border-radius: var(--r-md);
  background: var(--warning-soft); border: 1px solid var(--warning); color: var(--text); font-size: 13px; }
.daily-outside-days { display: flex; flex-wrap: wrap; gap: 6px; }
.daily-layout { display: grid; grid-template-columns: minmax(0, 1fr) minmax(280px, 380px); gap: 16px; align-items: start; }
@media (max-width: 900px) { .daily-layout { grid-template-columns: minmax(0, 1fr); } }
.daily-board { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.daily-default { display: flex; align-items: center; gap: 12px; padding: 10px 12px; text-align: left; cursor: pointer;
  border-radius: var(--r-md); border: 1px dashed var(--border); background: var(--surface); color: var(--text); }
.daily-default:hover { border-color: var(--brand-line); }
.daily-default-text { display: flex; flex-direction: column; gap: 2px; }
.daily-default-title { font-weight: 600; font-size: 13px; }
.daily-grid { display: grid; gap: 6px; }
.daily-cell { position: relative; display: flex; flex-direction: column; align-items: center; justify-content: flex-start;
  gap: 4px; min-height: 92px; padding: 18px 4px 6px; cursor: pointer; overflow: hidden;
  border-radius: var(--r-md); border: 1px solid var(--border-soft); background: var(--surface-inset); color: var(--text); }
.daily-cell:hover { border-color: var(--brand-line); }
.daily-cell.is-default .daily-slot { opacity: 0.45; filter: grayscale(0.6); }
.daily-cell.is-own { border: 2px solid var(--fam-reward); background: var(--fam-reward-soft); }
.daily-cell.is-selected, .daily-default.is-selected { outline: 2px solid var(--brand); outline-offset: 1px; }
.daily-cell-day { position: absolute; top: 4px; left: 6px; font-size: 11px; font-weight: 600; color: var(--text-mut); }
.daily-cell.is-own .daily-cell-day { color: var(--text); }
.daily-cell-stack { position: absolute; top: 3px; right: 6px; font-size: 12px; font-weight: 700; line-height: 1;
  color: var(--fam-reward); }
.daily-legend-stack::before { display: none; }
.daily-cell-title { max-width: 100%; font-size: 10px; text-align: center; }
.daily-slot { position: relative; display: inline-flex; align-items: center; justify-content: center; flex: none;
  width: 40px; height: 40px; border-radius: var(--r-xs); background: var(--mc-slot-bg);
  box-shadow: inset 2px 2px 0 var(--mc-slot-dark), inset -2px -2px 0 var(--mc-slot-light); }
.daily-slot img { width: 32px; height: 32px; object-fit: contain; image-rendering: pixelated; }
.daily-slot-name { font-size: 10px; color: var(--text-faint); }
.daily-slot-count { position: absolute; right: 2px; bottom: 0; font-size: 12px; font-weight: 700; color: var(--text);
  text-shadow: 1px 1px 0 var(--background); }
.daily-panel { position: sticky; top: 0; padding: 14px; border-radius: var(--r-lg); border: 1px solid var(--border-soft);
  background: var(--surface); }
.daily-panel-body { display: flex; flex-direction: column; gap: 14px; align-items: stretch; }
.daily-panel-title { font-weight: 600; font-size: 15px; }
.daily-fields { display: flex; flex-direction: column; gap: 14px; border: 0; padding: 0; margin: 0; min-width: 0; }
.daily-fields > div { display: flex; flex-direction: column; gap: 6px; }
.daily-inherited { display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: var(--r-md);
  background: var(--surface-inset); }
.daily-inherited-text { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.daily-milestones { display: flex; flex-direction: column; gap: 10px; padding-top: 12px; border-top: 1px solid var(--border-soft); }
.daily-milestone-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 8px; }
.daily-milestone { border-style: solid; }
.daily-milestone.is-invalid { border-color: var(--warning); }
.daily-milestone-add { display: flex; flex-wrap: wrap; gap: 8px; }
.daily-milestone-kind { display: flex; flex-wrap: wrap; gap: 6px; }
.daily-fields label { display: flex; flex-direction: column; gap: 6px; }
`;
de("daily", Ve);
const Ge = {
  id: "daily",
  displayName: "Daily rewards",
  schemaVersion: "daily/v1",
  sidebar: { icon: () => null, label: "Daily rewards", to: "/m/daily" },
  routes: [],
  screens: { rewards: $e },
  // The calendar menu previewed in the editor: its days, from the rewards draft.
  localPreviewRows: Oe
}, et = { ...Ge, sdk: "1.20.0" };
export {
  et as default
};
