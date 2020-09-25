package com.appboy.ui.contentcards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appboy.ui.R;

/**
 * This adapter displays a single, full width/height item. This item should denote that the Content Cards contains no items and is empty.
 */
public class AppboyEmptyContentCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_appboy_content_cards_empty, viewGroup, false);
    return new NetworkUnavailableViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
    // This method is here since all adapters require a bind implementation. This adapter is static, so no binding occurs.
  }

  @Override
  public int getItemCount() {
    return 1;
  }

  static class NetworkUnavailableViewHolder extends RecyclerView.ViewHolder {
    NetworkUnavailableViewHolder(View view) {
      super(view);
    }
  }
}
