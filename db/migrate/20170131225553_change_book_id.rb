class ChangeBookId < ActiveRecord::Migration[5.0]
  def change
    rename_column :list_items, :goodreads_book_id, :book_id
    change_column :list_items, :book_id, :string
  end
end
