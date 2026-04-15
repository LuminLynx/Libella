from contextlib import contextmanager

import psycopg
from psycopg.rows import dict_row

from .config import RESOLVED_DATABASE_URL


def _connect() -> psycopg.Connection:
    return psycopg.connect(RESOLVED_DATABASE_URL, row_factory=dict_row)


@contextmanager
def get_connection() -> psycopg.Connection:
    connection = _connect()
    try:
        yield connection
    finally:
        connection.close()
