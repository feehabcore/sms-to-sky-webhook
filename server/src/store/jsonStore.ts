import fs from 'fs';
import path from 'path';
import { DataStore, emptyStore } from '../types/store';

export class JsonStore {
  private filePath: string;
  private data: DataStore;

  constructor(dataDir: string) {
    fs.mkdirSync(dataDir, { recursive: true });
    this.filePath = path.join(dataDir, 'store.json');
    this.data = this.load();
  }

  private load(): DataStore {
    if (!fs.existsSync(this.filePath)) {
      const initial = emptyStore();
      this.write(initial);
      return initial;
    }
    try {
      const raw = fs.readFileSync(this.filePath, 'utf8');
      return { ...emptyStore(), ...JSON.parse(raw) } as DataStore;
    } catch {
      return emptyStore();
    }
  }

  private write(data: DataStore): void {
    const tmp = `${this.filePath}.tmp`;
    fs.writeFileSync(tmp, JSON.stringify(data, null, 2), 'utf8');
    fs.renameSync(tmp, this.filePath);
  }

  get snapshot(): DataStore {
    return this.data;
  }

  update(mutator: (draft: DataStore) => void): DataStore {
    mutator(this.data);
    this.write(this.data);
    return this.data;
  }
}
