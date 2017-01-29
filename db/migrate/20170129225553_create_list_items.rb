class CreateListItems < ActiveRecord::Migration[5.0]
  def change
    create_table :list_items do |t|
      t.references :list
      t.integer :goodreads_book_id

      t.timestamps
    end
  end
end
