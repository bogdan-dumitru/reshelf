class CreateUsers < ActiveRecord::Migration[5.0]
  def change
    create_table :users do |t|
      t.string :name
      t.string :token

      t.timestamps
      t.index :token
    end
  end
end
