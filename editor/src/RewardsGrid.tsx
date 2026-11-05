import {
  ActionChainEditor,
  type ConfigScreenProps,
  ItemValueInput,
  McText,
  projectLangs,
  textValue,
  useContentLang,
  useItemTexture,
  useProject,
} from "@artisan/editor-sdk";
import { type ReactNode, useState } from "react";
import { useT } from "./i18n";
import {
  type Cell,
  type Reward,
  type RewardsValue,
  WEEK,
  actionCount,
  asObject,
  cells,
  cycleLength,
  daysOutside,
  detachTexts,
  iconCount,
  iconMaterial,
  iconTitle,
  ownDays,
  withDay,
} from "./rewards";

type Selection = number | "default" | null;

/**
 * The Rewards screen of the daily folder: the cycle as a grid of weeks. A day with its own
 * reward stands out, the others show the reward of the other days, faded. A click opens the
 * day beside the grid: its icon and actions, or the button that gives it its own reward.
 */
export function RewardsGrid({ projectId, value, set, readOnly }: ConfigScreenProps) {
  const t = useT();
  const rewards = (asObject(value) ?? {}) as RewardsValue;
  const grid = cells(rewards);
  const outside = daysOutside(rewards);
  const fallback = asObject(rewards.default_day);
  const [selected, setSelected] = useState<Selection>(null);
  const project = useProject(projectId);
  // A literal title is the default language's text, as the generated form writes it.
  const defaultText = (key: string) =>
    project ? textValue(project, key, projectLangs(project)[0] ?? "en") : "";

  const setDay = (day: number, reward: Reward | undefined) => set(["days"], withDay(rewards, day, reward));

  return (
    <div className="daily-rewards">
      <div className="daily-toolbar">
        <label className="daily-cycle">
          <span className="field-label">{t("cycleLength")}</span>
          <input
            className="input daily-cycle-input"
            type="number"
            min={1}
            value={cycleLength(rewards)}
            disabled={readOnly}
            onChange={(e) => {
              const n = Number.parseInt(e.target.value, 10);
              if (Number.isInteger(n) && n >= 1) set(["cycle_length"], n);
            }}
          />
          <span className="daily-faint">{t("cycleHint")}</span>
        </label>
        <div className="daily-legend">
          <span className="daily-legend-own">{t("legendOwn")}</span>
          <span className="daily-legend-default">{t("legendDefault")}</span>
        </div>
      </div>

      {outside.length > 0 && (
        <div className="daily-outside" role="alert">
          <strong>{t("outsideTitle")}</strong>
          <span>{t("outsideHint")}</span>
          <div className="daily-outside-days">
            {outside.map((day) => (
              <button
                key={day}
                type="button"
                className="chip"
                disabled={readOnly}
                onClick={() => setDay(day, undefined)}
              >
                {t("remove", { day })} ×
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="daily-layout">
        <div className="daily-board">
          <button
            type="button"
            className={`daily-default${selected === "default" ? " is-selected" : ""}`}
            onClick={() => setSelected("default")}
          >
            <Slot reward={fallback} />
            <span className="daily-default-text">
              <span className="daily-default-title">{t("defaultTitle")}</span>
              <span className="daily-faint">{fallback ? t("defaultHint") : t("defaultNone")}</span>
            </span>
          </button>
          <div className="daily-grid" style={{ gridTemplateColumns: `repeat(${WEEK}, minmax(0, 1fr))` }}>
            {grid.map((cell) => (
              <DayCell
                key={cell.day}
                cell={cell}
                projectId={projectId}
                selected={selected === cell.day}
                onSelect={() => setSelected(cell.day)}
              />
            ))}
          </div>
        </div>

        <aside className="daily-panel">
          {selected === null ? (
            <p className="daily-faint">{t("pick")}</p>
          ) : selected === "default" ? (
            <Panel title={t("defaultTitle")} hint={fallback ? t("defaultHint") : t("defaultNone")}>
              {fallback ? (
                <>
                  <RewardFields
                    projectId={projectId}
                    reward={fallback}
                    readOnly={readOnly}
                    onChange={(path, next) => set(["default_day", ...path], next)}
                  />
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    disabled={readOnly}
                    onClick={() => set(["default_day"], undefined)}
                  >
                    {t("defaultRemove")}
                  </button>
                </>
              ) : (
                <button
                  type="button"
                  className="btn btn-sm"
                  disabled={readOnly}
                  onClick={() => set(["default_day"], { icon: { material: "BREAD" }, actions: [] })}
                >
                  {t("defaultAdd")}
                </button>
              )}
            </Panel>
          ) : (
            <DayPanel
              day={selected}
              projectId={projectId}
              own={ownDays(rewards).get(selected)}
              fallback={fallback}
              readOnly={readOnly}
              onCustomize={() =>
                setDay(selected, detachTexts(fallback, defaultText) ?? { icon: { material: "CHEST" }, actions: [] })
              }
              onReset={() => setDay(selected, undefined)}
              onChange={(path, next) => set(["days", dayKey(rewards, selected), ...path], next)}
              onEditDefault={() => setSelected("default")}
            />
          )}
        </aside>
      </div>
    </div>
  );
}

/** The key `days` holds the day under, as written (`7` or `"7"`). */
function dayKey(value: RewardsValue, day: number): string {
  return Object.keys(asObject(value.days) ?? {}).find((k) => Number(k) === day) ?? String(day);
}

function DayCell({
  cell,
  projectId,
  selected,
  onSelect,
}: {
  cell: Cell;
  projectId: string;
  selected: boolean;
  onSelect: () => void;
}) {
  const t = useT();
  const title = useIconTitle(projectId, cell.reward?.icon);
  const classes = ["daily-cell", cell.own ? "is-own" : "is-default", selected ? "is-selected" : ""];
  return (
    <button type="button" className={classes.join(" ")} onClick={onSelect} title={t("day", { day: cell.day })}>
      <span className="daily-cell-day">{cell.day}</span>
      <Slot reward={cell.reward} />
      {cell.own && title && (
        <span className="daily-cell-title">
          <McText source={title} maxLines={2} align="center" scale={1} />
        </span>
      )}
    </button>
  );
}

/** An inventory slot with the reward's icon and its count. */
function Slot({ reward }: { reward: Reward | undefined }) {
  const icon = reward?.icon;
  const material = iconMaterial(icon);
  const texture = useItemTexture(material);
  const count = iconCount(icon);
  return (
    <span className="daily-slot">
      {texture ? (
        <img src={texture} alt={material} draggable={false} />
      ) : (
        <span className="daily-slot-name">{material ? material.slice(0, 3) : "—"}</span>
      )}
      {count > 1 && <span className="daily-slot-count">{count}</span>}
    </span>
  );
}

function useIconTitle(projectId: string, icon: unknown): string {
  const project = useProject(projectId);
  const lang = useContentLang();
  return iconTitle(icon, lang, (key) => (project ? textValue(project, key, lang) : ""));
}

function Panel({ title, hint, children }: { title: string; hint: string; children: ReactNode }) {
  return (
    <div className="daily-panel-body">
      <div>
        <div className="daily-panel-title">{title}</div>
        <div className="daily-faint">{hint}</div>
      </div>
      {children}
    </div>
  );
}

function DayPanel({
  day,
  projectId,
  own,
  fallback,
  readOnly,
  onCustomize,
  onReset,
  onChange,
  onEditDefault,
}: {
  day: number;
  projectId: string;
  own: Reward | undefined;
  fallback: Reward | undefined;
  readOnly: boolean;
  onCustomize: () => void;
  onReset: () => void;
  onChange: (path: string[], value: unknown) => void;
  onEditDefault: () => void;
}) {
  const t = useT();
  const title = useIconTitle(projectId, (own ?? fallback)?.icon);
  if (!own) {
    const steps = actionCount(fallback);
    return (
      <Panel title={t("day", { day })} hint={t("inheritHint")}>
        <div className="daily-inherited">
          <Slot reward={fallback} />
          <span className="daily-inherited-text">
            {title ? <McText source={title} scale={1} /> : <span className="daily-faint">{t("defaultNone")}</span>}
            <span className="daily-faint">{steps > 0 ? t("steps", { count: steps }) : t("noActions")}</span>
          </span>
        </div>
        <button type="button" className="btn btn-sm" disabled={readOnly} onClick={onCustomize}>
          {t("customize")}
        </button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={onEditDefault}>
          {t("editDefault")}
        </button>
      </Panel>
    );
  }
  return (
    <Panel title={t("day", { day })} hint={t("ownHint")}>
      <RewardFields projectId={projectId} reward={own} readOnly={readOnly} onChange={onChange} />
      <button type="button" className="btn btn-ghost btn-sm" disabled={readOnly} onClick={onReset}>
        {t("reset")}
      </button>
    </Panel>
  );
}

function RewardFields({
  projectId,
  reward,
  readOnly,
  onChange,
}: {
  projectId: string;
  reward: Reward;
  readOnly: boolean;
  onChange: (path: string[], value: unknown) => void;
}) {
  const t = useT();
  return (
    <fieldset className="daily-fields" disabled={readOnly}>
      <div>
        <div className="field-label">{t("icon")}</div>
        <div className="daily-faint">{t("iconHint")}</div>
        <ItemValueInput projectId={projectId} value={reward.icon} onChange={(next) => onChange(["icon"], next)} />
      </div>
      <div>
        <div className="field-label">{t("actions")}</div>
        <div className="daily-faint">{t("actionsHint")}</div>
        <ActionChainEditor
          projectId={projectId}
          value={Array.isArray(reward.actions) ? reward.actions : []}
          onChange={(next) => onChange(["actions"], next)}
        />
      </div>
    </fieldset>
  );
}
