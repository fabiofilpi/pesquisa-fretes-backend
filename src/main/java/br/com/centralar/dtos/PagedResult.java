package br.com.centralar.dtos;

import java.util.List;

public record PagedResult<T>(List<T> items, long total, int page, int size, int pageCount) {

  public boolean hasNext() {
    return page < pageCount - 1;
  }

  public boolean hasPrev() {
    return page > 0;
  }
}
