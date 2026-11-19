import { createModuleI18n as te, useProject as Y, useItemTexture as ne, McText as F, ItemValueInput as ie, ActionChainEditor as ae, useContentLang as se, textValue as J, projectLangs as le, injectModuleStyles as re } from "@artisan/editor-sdk";
import { jsxs as u, jsx as s, Fragment as W } from "react/jsx-runtime";
import { useState as oe } from "react";
const de = 28, ce = 7;
function y(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? e : void 0;
}
function S(e) {
  const t = Number(e.cycle_length);
  return Number.isInteger(t) && t >= 1 ? t : de;
}
function C(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [n, a] of Object.entries(y(e.days) ?? {})) {
    const i = Number(n);
    Number.isInteger(i) && i >= 1 && t.set(i, y(a) ?? {});
  }
  return t;
}
function ue(e) {
  const t = S(e), n = C(e), a = y(e.default_day);
  return Array.from({ length: t }, (i, l) => {
    const r = l + 1, o = n.get(r);
    return { day: r, own: o !== void 0, reward: o ?? a };
  });
}
function me(e) {
  const t = S(e);
  return [...C(e).keys()].filter((n) => n > t).sort((n, a) => n - a);
}
function fe(e, t, n) {
  const a = Object.entries(y(e.days) ?? {}).filter(([i]) => Number(i) !== t);
  return n !== void 0 && a.push([String(t), n]), a.sort(([i], [l]) => Number(i) - Number(l)), Object.fromEntries(a);
}
function pe(e) {
  return e === void 0 ? e : JSON.parse(JSON.stringify(e));
}
function ye(e) {
  if (typeof e == "string") return e;
  const t = y(e), n = t?.extends ?? t?.item ?? t?.material;
  return typeof n == "string" ? n : "";
}
function he(e) {
  const t = y(e)?.count;
  return typeof t == "number" && t > 1 ? t : 1;
}
function ge(e) {
  return Array.isArray(e?.actions) ? e.actions.length : 0;
}
function be(e, t, n) {
  const a = y(e)?.title;
  if (typeof a == "string") return a;
  const i = y(a);
  if (!i) return "";
  if (typeof i.key == "string") return n(i.key);
  const l = i[t];
  if (typeof l == "string") return l;
  const r = Object.values(i).find((o) => typeof o == "string");
  return typeof r == "string" ? r : "";
}
function ve(e, t) {
  const n = pe(e), a = y(n?.icon);
  if (!a) return n;
  for (const i of ["title", "lore"]) {
    const l = y(a[i]);
    l && typeof l.key == "string" && (a[i] = t(l.key));
  }
  return n;
}
function M(e) {
  const t = Number(e);
  return Number.isInteger(t) && t >= 1 ? t : 0;
}
function V(e) {
  const t = e.milestones;
  return Array.isArray(t) ? t.map((n) => y(n) ?? {}) : [];
}
function I(e) {
  const t = M(e.at), n = M(e.every);
  if (t > 0) return { at: t, every: 0, from: 0, icon: e.icon, actions: e.actions };
  if (n > 0) return { at: 0, every: n, from: M(e.from) || n, icon: e.icon, actions: e.actions };
}
function _(e) {
  return V(e).map(I).filter((t) => t !== void 0);
}
function xe(e, t) {
  return t < 1 ? !1 : e.every === 0 ? t === e.at : t >= e.from && (t - e.from) % e.every === 0;
}
function z(e, t) {
  const n = Math.max(t, 1);
  return e.every === 0 ? e.at >= n ? e.at : -1 : n <= e.from ? e.from : e.from + Math.ceil((n - e.from) / e.every) * e.every;
}
function Ne(e, t) {
  return e.filter((n) => xe(n, t));
}
function U(e, t, n) {
  const a = [], i = e.map((l) => z(l, t));
  for (; a.length < n; ) {
    const l = i.filter((o) => o > 0);
    if (l.length === 0) break;
    const r = Math.min(...l);
    for (let o = 0; o < i.length && a.length < n; o++) {
      if (i[o] !== r) continue;
      const f = e[o];
      a.push({ streak: r, milestone: f }), i[o] = f.every > 0 ? z(f, r + 1) : -1;
    }
  }
  return a;
}
function we(e) {
  return e.length > 0 && !e.some((t) => t.every > 0);
}
function B(e, t) {
  return e.every === 0 ? t("milestoneAt", { at: e.at }) : e.from === e.every ? t("milestoneEvery", { every: e.every }) : t("milestoneEveryFrom", { every: e.every, from: e.from });
}
const ke = "daily:calendar", $ = "daily:status", q = "daily:milestones", Ae = 28, _e = 200, k = 5, Te = "rewards", Ee = 7, De = 20, K = 35, E = { material: "PAPER" }, G = 864e5;
function X(e) {
  const t = e?.dataSources?.find(
    (n) => String(n.id) === Te
  );
  if (!(!t || t.shape !== "config"))
    return y(t.value);
}
function Se(e) {
  if (X(e)) return !0;
  const t = e;
  return t?.ownerModule === "daily" || String(t?.id ?? "") === "daily" || String(t?.id ?? "").endsWith(":daily");
}
function A(e, t) {
  return e < 1 ? 1 : (e - 1) % t + 1;
}
function Q(e, t) {
  if (!(t < 1))
    return C(e).get(t) ?? y(e.default_day);
}
function T(e, t, n) {
  const a = e?.[t];
  if (a === void 0) return n;
  const i = Number.parseInt(String(a).trim(), 10);
  return Number.isNaN(i) ? n : i;
}
function Ce(e, t) {
  return t < 2 ? 0 : (e - 1) % t;
}
function Re(e, t, n) {
  const a = [
    { status: "claimed", streak: 1 },
    { status: "claimed", streak: 2 },
    { status: "claimed", streak: 3 },
    { status: "missed", streak: 0 },
    { status: "available", streak: 4 }
  ], i = [];
  for (let r = -e; r < 0; r++) {
    const o = a[a.length + r];
    i.push(
      o ? {
        offset: r,
        status: o.status,
        cycleDay: o.streak > 0 ? A(o.streak, n) : 0,
        streak: o.streak
      } : { offset: r, status: "before", cycleDay: 0, streak: 0 }
    );
  }
  const l = k;
  i.push({ offset: 0, status: "available", cycleDay: A(l, n), streak: l });
  for (let r = 1; r <= t; r++)
    i.push({ offset: r, status: "upcoming", cycleDay: A(l + r, n), streak: l + r });
  return i;
}
function w(e) {
  return String(e).padStart(2, "0");
}
function je(e, t, n) {
  const a = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + e.offset * G), i = Q(t, e.cycleDay);
  return {
    id: `${a.getUTCFullYear()}-${w(a.getUTCMonth() + 1)}-${w(a.getUTCDate())}`,
    date: `${w(a.getUTCDate())}/${w(a.getUTCMonth() + 1)}`,
    offset: e.offset,
    status: e.status,
    is_today: e.offset === 0,
    is_claimed: e.status === "claimed",
    is_missed: e.status === "missed",
    is_before: e.status === "before",
    is_available: e.status === "available",
    is_upcoming: e.status === "upcoming",
    cycle_day: e.cycleDay,
    has_reward: i !== void 0,
    icon: i?.icon ?? E,
    // The sample player's streak never broke: each streak that a milestone matches pays it.
    ...Z(_(t), e.streak, e.streak > 0, i?.icon ?? E)
  };
}
function Z(e, t, n, a) {
  const i = n ? Ne(e, t) : [];
  return {
    streak: t,
    is_milestone: i.length > 0,
    milestone_count: i.length,
    milestone_icon: i.find((l) => l.icon != null)?.icon ?? a
  };
}
function He(e, t, n) {
  const a = _(e);
  return U(a, k, t).map(({ streak: i, milestone: l }) => {
    const r = i - k, o = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + r * G);
    return {
      id: `${i}-${a.indexOf(l) + 1}`,
      streak: i,
      offset: r,
      date: `${w(o.getUTCDate())}/${w(o.getUTCMonth() + 1)}`,
      is_today: r === 0,
      repeats: l.every > 0,
      every: l.every,
      icon: l.icon ?? E
    };
  });
}
function Me(e, t) {
  const n = Q(t, Number(e.cycle_day) || 0), a = n?.icon ?? E, i = Z(_(t), Number(e.streak) || 0, e.is_milestone === !0, a);
  return { ...e, has_reward: n !== void 0, icon: a, ...i, is_milestone: e.is_milestone === !0 };
}
function Le(e, t = {}) {
  const n = U(_(t), k, 1)[0];
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
function Fe(e, t, n, a = /* @__PURE__ */ new Date()) {
  if (t !== ke && t !== $ && t !== q || !Se(e)) return null;
  const i = X(e) ?? {}, l = S(i), r = n?.serverRows;
  if (t === $) {
    const c = r?.filter((p) => p.loaded === !0) ?? [];
    return c.length > 0 ? c.map((p) => ({ ...p, cycle_length: l })) : [Le(l, i)];
  }
  if (t === q) {
    if (r && r.length > 0) return r;
    const c = Math.max(0, Math.min(_e, T(n?.params, "count", Ae)));
    return He(i, c, a);
  }
  if (r && r.length > 0) return r.map((c) => Me(c, i));
  const o = Math.max(0, Math.min(K, T(n?.params, "past", Ee))), f = Math.max(0, T(n?.params, "future", De)), d = Math.min(Ce(A(k, l), T(n?.params, "week", 0)), f, K - o);
  return Re(o + d, f - d, l).map((c) => je(c, i, a));
}
const Ie = {
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
}, Ue = {
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
}, v = te({ en: Ie, fr: Ue });
function Oe({ projectId: e, value: t, set: n, readOnly: a }) {
  const i = v(), l = y(t) ?? {}, r = ue(l), o = me(l), f = y(l.default_day), [d, c] = oe(null), p = Y(e), x = (m) => p ? J(p, m, le(p)[0] ?? "en") : "", N = (m, h) => n(["days"], fe(l, m, h)), g = V(l), b = (m) => n(["milestones"], m.length > 0 ? m : void 0), j = (m) => {
    b([...g, m]), c({ milestone: g.length });
  }, H = typeof d == "object" && d !== null ? d.milestone : -1;
  return /* @__PURE__ */ u("div", { className: "daily-rewards", children: [
    /* @__PURE__ */ u("div", { className: "daily-toolbar", children: [
      /* @__PURE__ */ u("label", { className: "daily-cycle", children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: i("cycleLength") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input daily-cycle-input",
            type: "number",
            min: 1,
            value: S(l),
            disabled: a,
            onChange: (m) => {
              const h = Number.parseInt(m.target.value, 10);
              Number.isInteger(h) && h >= 1 && n(["cycle_length"], h);
            }
          }
        ),
        /* @__PURE__ */ s("span", { className: "daily-faint", children: i("cycleHint") })
      ] }),
      /* @__PURE__ */ u("div", { className: "daily-legend", children: [
        /* @__PURE__ */ s("span", { className: "daily-legend-own", children: i("legendOwn") }),
        /* @__PURE__ */ s("span", { className: "daily-legend-default", children: i("legendDefault") })
      ] })
    ] }),
    o.length > 0 && /* @__PURE__ */ u("div", { className: "daily-outside", role: "alert", children: [
      /* @__PURE__ */ s("strong", { children: i("outsideTitle") }),
      /* @__PURE__ */ s("span", { children: i("outsideHint") }),
      /* @__PURE__ */ s("div", { className: "daily-outside-days", children: o.map((m) => /* @__PURE__ */ u(
        "button",
        {
          type: "button",
          className: "chip",
          disabled: a,
          onClick: () => N(m, void 0),
          children: [
            i("remove", { day: m }),
            " ×"
          ]
        },
        m
      )) })
    ] }),
    /* @__PURE__ */ u("div", { className: "daily-layout", children: [
      /* @__PURE__ */ u("div", { className: "daily-board", children: [
        /* @__PURE__ */ u(
          "button",
          {
            type: "button",
            className: `daily-default${d === "default" ? " is-selected" : ""}`,
            onClick: () => c("default"),
            children: [
              /* @__PURE__ */ s(R, { reward: f }),
              /* @__PURE__ */ u("span", { className: "daily-default-text", children: [
                /* @__PURE__ */ s("span", { className: "daily-default-title", children: i("defaultTitle") }),
                /* @__PURE__ */ s("span", { className: "daily-faint", children: i(f ? "defaultHint" : "defaultNone") })
              ] })
            ]
          }
        ),
        /* @__PURE__ */ s("div", { className: "daily-grid", style: { gridTemplateColumns: `repeat(${ce}, minmax(0, 1fr))` }, children: r.map((m) => /* @__PURE__ */ s(
          ze,
          {
            cell: m,
            projectId: e,
            selected: d === m.day,
            onSelect: () => c(m.day)
          },
          m.day
        )) }),
        /* @__PURE__ */ s(
          qe,
          {
            rewards: l,
            entries: g,
            projectId: e,
            selected: H,
            readOnly: a,
            onSelect: (m) => c({ milestone: m }),
            onAdd: j
          }
        )
      ] }),
      /* @__PURE__ */ s("aside", { className: "daily-panel", children: d === null ? /* @__PURE__ */ s("p", { className: "daily-faint", children: i("pick") }) : typeof d == "object" ? g[d.milestone] ? /* @__PURE__ */ s(
        Ye,
        {
          projectId: e,
          entry: g[d.milestone],
          readOnly: a,
          onChange: (m) => b(g.map((h, ee) => ee === d.milestone ? m : h)),
          onRemove: () => {
            b(g.filter((m, h) => h !== d.milestone)), c(null);
          }
        }
      ) : /* @__PURE__ */ s("p", { className: "daily-faint", children: i("pick") }) : d === "default" ? /* @__PURE__ */ s(D, { title: i("defaultTitle"), hint: i(f ? "defaultHint" : "defaultNone"), children: f ? /* @__PURE__ */ u(W, { children: [
        /* @__PURE__ */ s(
          P,
          {
            projectId: e,
            reward: f,
            readOnly: a,
            onChange: (m, h) => n(["default_day", ...m], h)
          }
        ),
        /* @__PURE__ */ s(
          "button",
          {
            type: "button",
            className: "btn btn-ghost btn-sm",
            disabled: a,
            onClick: () => n(["default_day"], void 0),
            children: i("defaultRemove")
          }
        )
      ] }) : /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: a,
          onClick: () => n(["default_day"], { icon: { material: "BREAD" }, actions: [] }),
          children: i("defaultAdd")
        }
      ) }) : /* @__PURE__ */ s(
        $e,
        {
          day: d,
          projectId: e,
          own: C(l).get(d),
          fallback: f,
          readOnly: a,
          onCustomize: () => N(d, ve(f, x) ?? { icon: { material: "CHEST" }, actions: [] }),
          onReset: () => N(d, void 0),
          onChange: (m, h) => n(["days", Pe(l, d), ...m], h),
          onEditDefault: () => c("default")
        }
      ) })
    ] })
  ] });
}
function Pe(e, t) {
  return Object.keys(y(e.days) ?? {}).find((n) => Number(n) === t) ?? String(t);
}
function ze({
  cell: e,
  projectId: t,
  selected: n,
  onSelect: a
}) {
  const i = v(), l = O(t, e.reward?.icon), r = ["daily-cell", e.own ? "is-own" : "is-default", n ? "is-selected" : ""];
  return /* @__PURE__ */ u("button", { type: "button", className: r.join(" "), onClick: a, title: i("day", { day: e.day }), children: [
    /* @__PURE__ */ s("span", { className: "daily-cell-day", children: e.day }),
    /* @__PURE__ */ s(R, { reward: e.reward }),
    e.own && l && /* @__PURE__ */ s("span", { className: "daily-cell-title", children: /* @__PURE__ */ s(F, { source: l, maxLines: 2, align: "center", scale: 1 }) })
  ] });
}
function R({ reward: e }) {
  const t = e?.icon, n = ye(t), a = ne(n), i = he(t);
  return /* @__PURE__ */ u("span", { className: "daily-slot", children: [
    a ? /* @__PURE__ */ s("img", { src: a, alt: n, draggable: !1 }) : /* @__PURE__ */ s("span", { className: "daily-slot-name", children: n ? n.slice(0, 3) : "—" }),
    i > 1 && /* @__PURE__ */ s("span", { className: "daily-slot-count", children: i })
  ] });
}
function O(e, t) {
  const n = Y(e), a = se();
  return be(t, a, (i) => n ? J(n, i, a) : "");
}
function D({ title: e, hint: t, children: n }) {
  return /* @__PURE__ */ u("div", { className: "daily-panel-body", children: [
    /* @__PURE__ */ u("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: e }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: t })
    ] }),
    n
  ] });
}
function $e({
  day: e,
  projectId: t,
  own: n,
  fallback: a,
  readOnly: i,
  onCustomize: l,
  onReset: r,
  onChange: o,
  onEditDefault: f
}) {
  const d = v(), c = O(t, (n ?? a)?.icon);
  if (!n) {
    const p = ge(a);
    return /* @__PURE__ */ u(D, { title: d("day", { day: e }), hint: d("inheritHint"), children: [
      /* @__PURE__ */ u("div", { className: "daily-inherited", children: [
        /* @__PURE__ */ s(R, { reward: a }),
        /* @__PURE__ */ u("span", { className: "daily-inherited-text", children: [
          c ? /* @__PURE__ */ s(F, { source: c, scale: 1 }) : /* @__PURE__ */ s("span", { className: "daily-faint", children: d("defaultNone") }),
          /* @__PURE__ */ s("span", { className: "daily-faint", children: p > 0 ? d("steps", { count: p }) : d("noActions") })
        ] })
      ] }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-sm", disabled: i, onClick: l, children: d("customize") }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", onClick: f, children: d("editDefault") })
    ] });
  }
  return /* @__PURE__ */ u(D, { title: d("day", { day: e }), hint: d("ownHint"), children: [
    /* @__PURE__ */ s(P, { projectId: t, reward: n, readOnly: i, onChange: o }),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: i, onClick: r, children: d("reset") })
  ] });
}
function P({
  projectId: e,
  reward: t,
  readOnly: n,
  iconHint: a,
  actionsHint: i,
  onChange: l
}) {
  const r = v();
  return /* @__PURE__ */ u("fieldset", { className: "daily-fields", disabled: n, children: [
    /* @__PURE__ */ u("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: r("icon") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: a ?? r("iconHint") }),
      /* @__PURE__ */ s(ie, { projectId: e, value: t.icon, onChange: (o) => l(["icon"], o) })
    ] }),
    /* @__PURE__ */ u("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: r("actions") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: i ?? r("actionsHint") }),
      /* @__PURE__ */ s(
        ae,
        {
          projectId: e,
          value: Array.isArray(t.actions) ? t.actions : [],
          onChange: (o) => l(["actions"], o)
        }
      )
    ] })
  ] });
}
function qe({
  rewards: e,
  entries: t,
  projectId: n,
  selected: a,
  readOnly: i,
  onSelect: l,
  onAdd: r
}) {
  const o = v(), f = _(e), d = U(f, 1, 8).map((c) => c.streak);
  return /* @__PURE__ */ u("section", { className: "daily-milestones", children: [
    /* @__PURE__ */ u("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: o("milestonesTitle") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: o("milestonesHint") })
    ] }),
    we(f) && /* @__PURE__ */ s("div", { className: "daily-outside", role: "alert", children: o("milestoneNoRepeat") }),
    t.length === 0 ? /* @__PURE__ */ s("span", { className: "daily-faint", children: o("milestonesNone") }) : /* @__PURE__ */ s("div", { className: "daily-milestone-list", children: t.map((c, p) => /* @__PURE__ */ s(
      Ke,
      {
        entry: c,
        projectId: n,
        selected: a === p,
        onSelect: () => l(p)
      },
      p
    )) }),
    d.length > 0 && /* @__PURE__ */ s("span", { className: "daily-faint", children: o("milestoneNext", { list: d.join(", ") }) }),
    /* @__PURE__ */ u("div", { className: "daily-milestone-add", children: [
      /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: i,
          onClick: () => r({ at: 30, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: o("milestoneAddAt")
        }
      ),
      /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: i,
          onClick: () => r({ every: 50, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: o("milestoneAddEvery")
        }
      )
    ] })
  ] });
}
function Ke({
  entry: e,
  projectId: t,
  selected: n,
  onSelect: a
}) {
  const i = v(), l = O(t, e.icon), r = I(e);
  return /* @__PURE__ */ u("button", { type: "button", className: ["daily-default", "daily-milestone", n ? "is-selected" : "", r ? "" : "is-invalid"].join(" "), onClick: a, children: [
    /* @__PURE__ */ s(R, { reward: e }),
    /* @__PURE__ */ u("span", { className: "daily-default-text", children: [
      /* @__PURE__ */ s("span", { className: "daily-default-title", children: r ? B(r, i) : i("milestoneInvalid") }),
      l && /* @__PURE__ */ s(F, { source: l, maxLines: 1, scale: 1 })
    ] })
  ] });
}
function L(e) {
  const t = Number.parseInt(e, 10);
  return Number.isInteger(t) && t >= 1 ? t : void 0;
}
function Ye({
  projectId: e,
  entry: t,
  readOnly: n,
  onChange: a,
  onRemove: i
}) {
  const l = v(), r = I(t), o = t.at === void 0 && t.every !== void 0, f = (c, p) => {
    const x = { ...t, [c]: p };
    p === void 0 && delete x[c];
    const { at: N, every: g, from: b, ...j } = x;
    return Object.fromEntries(
      Object.entries({ at: N, every: g, from: b, ...j }).filter(([, H]) => H !== void 0)
    );
  }, d = (c) => {
    if (c === o) return;
    const { at: p, every: x, from: N, ...g } = t, b = Number(p) || Number(x) || 30;
    a(c ? { every: b, ...g } : { at: b, ...g });
  };
  return /* @__PURE__ */ u(D, { title: r ? B(r, l) : l("milestoneInvalid"), hint: l("milestonesHint"), children: [
    /* @__PURE__ */ u("fieldset", { className: "daily-fields", disabled: n, children: [
      /* @__PURE__ */ u("div", { children: [
        /* @__PURE__ */ s("div", { className: "field-label", children: l("milestoneKind") }),
        /* @__PURE__ */ u("div", { className: "daily-milestone-kind", children: [
          /* @__PURE__ */ s("button", { type: "button", className: `chip${o ? "" : " is-active"}`, onClick: () => d(!1), children: l("milestoneOnce") }),
          /* @__PURE__ */ s("button", { type: "button", className: `chip${o ? " is-active" : ""}`, onClick: () => d(!0), children: l("milestoneRepeat") })
        ] })
      ] }),
      o ? /* @__PURE__ */ u(W, { children: [
        /* @__PURE__ */ u("label", { children: [
          /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneEveryLabel") }),
          /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.every ?? ""),
              onChange: (c) => a(f("every", L(c.target.value)))
            }
          )
        ] }),
        /* @__PURE__ */ u("label", { children: [
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
              onChange: (c) => a(f("from", L(c.target.value)))
            }
          )
        ] })
      ] }) : /* @__PURE__ */ u("label", { children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneAtLabel") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input",
            type: "number",
            min: 1,
            value: String(t.at ?? ""),
            onChange: (c) => a(f("at", L(c.target.value)))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ s(
      P,
      {
        projectId: e,
        reward: t,
        readOnly: n,
        iconHint: l("milestoneIconHint"),
        actionsHint: l("milestoneActionsHint"),
        onChange: (c, p) => a(f(c[0], p))
      }
    ),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: n, onClick: i, children: l("milestoneRemove") })
  ] });
}
const Je = `
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
re("daily", Je);
const We = {
  id: "daily",
  displayName: "Daily rewards",
  schemaVersion: "daily/v1",
  sidebar: { icon: () => null, label: "Daily rewards", to: "/m/daily" },
  routes: [],
  screens: { rewards: Oe },
  // The calendar menu previewed in the editor: its days, from the rewards draft.
  localPreviewRows: Fe
}, Xe = { ...We, sdk: "1.20.0" };
export {
  Xe as default
};
