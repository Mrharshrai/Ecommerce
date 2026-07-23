import React from 'react';
import {
  Calendar,
  User,
  Store,
  Layers,
  Users,
  Baby,
  ShieldCheck,
  Barcode,
  Globe,
  RotateCcw,
  FileText,
  List,
  Info,
  Tag,
  Star,
  Edit2,
  EyeOff,
  PowerOff,
  Trash2,
  Copy } from
'lucide-react';
export function OverviewTab() {
  return (
    <div className="flex flex-col gap-6">
      {/* Top Main Card */}
      <div className="bg-white rounded-2xl p-6 shadow-card border border-black/5 flex flex-col lg:flex-row gap-8">
        {/* Left: Image Gallery */}
        <div className="w-full lg:w-[320px] shrink-0 flex flex-col gap-4">
          <div className="aspect-square bg-[#f5f2ec] rounded-2xl overflow-hidden flex items-center justify-center p-4">
            <img
              src="https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=600&q=80"
              alt="Premium leather hand bag"
              className="w-full h-full object-contain mix-blend-multiply" />
            
          </div>
          <div className="flex gap-2">
            {[1, 2, 3, 4].map((i) =>
            <div
              key={i}
              className={`w-14 h-14 rounded-lg bg-[#f5f2ec] border-2 ${i === 1 ? 'border-brown' : 'border-transparent'} overflow-hidden`}>
              
                <img
                src="https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=200&q=80"
                alt={`Bag view ${i}`}
                className="w-full h-full object-contain mix-blend-multiply" />
              
              </div>
            )}
            <div className="w-14 h-14 rounded-lg bg-gray-50 border border-gray-200 flex items-center justify-center text-gray-500 font-medium text-sm">
              +3
            </div>
          </div>
        </div>

        {/* Right: Details */}
        <div className="flex-1 flex flex-col">
          {/* Header Actions */}
          <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div className="flex items-center gap-3">
              <Badge
                type="green"
                icon={<div className="w-2 h-2 rounded-full bg-current" />}>
                
                ACTIVE
              </Badge>
              <Badge type="blue" icon={<EyeOff className="w-3.5 h-3.5" />}>
                PUBLISHED
              </Badge>
            </div>
            <div className="flex items-center gap-2">
              <ActionButton icon={<Edit2 className="w-4 h-4" />} primary>
                Edit Product
              </ActionButton>
              <ActionButton icon={<EyeOff className="w-4 h-4" />}>
                Unpublish
              </ActionButton>
              <ActionButton icon={<PowerOff className="w-4 h-4" />}>
                Deactivate
              </ActionButton>
              <ActionButton icon={<Trash2 className="w-4 h-4" />} danger>
                Delete
              </ActionButton>
            </div>
          </div>

          {/* Title */}
          <div className="mb-8">
            <h1 className="text-4xl font-serif font-bold text-gray-900 mb-2">
              bag
            </h1>
            <p className="text-gray-500">Premium leather hand bag</p>
          </div>

          {/* Info Grid */}
          <div className="flex flex-wrap gap-4 mb-8">
            <InfoBox label="ASIN" value="BAG12345" />
            <InfoBox label="Tag ID" value="1" />
            <InfoBox label="Brand" value="brand" />
            <InfoBox label="Category" value="BAG / hand bag" />
            <InfoBox label="Fingerprint" value="BAG12345" />
          </div>

          <div className="h-px w-full bg-gray-100 mb-6" />

          {/* Meta Info */}
          <div className="flex flex-wrap gap-8 text-sm">
            <MetaItem
              icon={<Calendar />}
              label="Created"
              value="12 May 2025, 10:30 AM" />
            
            <MetaItem
              icon={<Calendar />}
              label="Last Updated"
              value="12 May 2025, 02:45 PM" />
            
            <MetaItem icon={<User />} label="Created By" value="Admin" />
            <MetaItem icon={<User />} label="Updated By" value="Admin" />
          </div>
        </div>
      </div>

      {/* Middle Row: Attributes */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        <AttributeCard icon={<div />} label="CATEGORY" value="BAG / hand bag" />
        <AttributeCard icon={<Store />} label="BRAND" value="brand" />
        <AttributeCard icon={<Layers />} label="MATERIAL" value="leather" />
        <AttributeCard icon={<Users />} label="GENDER" value="UNISEX" />
        <AttributeCard icon={<Baby />} label="AGE GROUP" value="Infant_0_12M" />
      </div>

      {/* Bottom Row: Details */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Overview List */}
        <div className="bg-white rounded-2xl p-6 shadow-card border border-black/5">
          <h3 className="font-semibold text-lg mb-6">Overview</h3>
          <div className="space-y-5">
            <ListItem
              icon={<ShieldCheck />}
              label="ASIN"
              value="BAG12345"
              copyable />
            
            <ListItem
              icon={<Barcode />}
              label="EAN"
              value="8901234567890"
              copyable />
            
            <ListItem
              icon={<div />}
              label="HSN Code"
              value="42022210"
              copyable />
            
            <ListItem
              icon={<Globe />}
              label="Country of Origin"
              value="India" />
            
            <ListItem
              icon={<RotateCcw />}
              label="Return Policy"
              value="7 Days Returnable" />
            
            <ListItem
              icon={<ShieldCheck />}
              label="Warranty"
              value="No Warranty" />
            
          </div>
        </div>

        {/* Descriptions */}
        <div className="bg-white rounded-2xl p-6 shadow-card border border-black/5 flex flex-col gap-4">
          <ContentBox icon={<FileText />} title="Description">
            <p className="text-gray-600 text-sm">sdfgsdfg</p>
          </ContentBox>
          <ContentBox icon={<List />} title="Key Features">
            <ul className="list-disc list-inside text-gray-600 text-sm space-y-1">
              <li>sdfgsdfg</li>
              <li>sdfgsdfgsdfg</li>
            </ul>
          </ContentBox>
          <ContentBox icon={<Info />} title="Additional Notes">
            <p className="text-gray-600 text-sm">No additional notes</p>
          </ContentBox>
        </div>

        {/* Tags & Highlights */}
        <div className="bg-white rounded-2xl p-6 shadow-card border border-black/5">
          <h3 className="font-semibold text-lg mb-6">Tags & Highlights</h3>

          <div className="mb-8">
            <h4 className="text-sm text-gray-500 mb-3">Tags</h4>
            <div className="flex flex-wrap gap-2">
              <TagBadge icon={<Tag className="w-3.5 h-3.5" />}>
                sdfgsdfg
              </TagBadge>
              <TagBadge icon={<Tag className="w-3.5 h-3.5" />}>
                sdfgsdfgsdfg
              </TagBadge>
            </div>
          </div>

          <div>
            <h4 className="text-sm text-gray-500 mb-3">Highlights</h4>
            <div className="flex flex-wrap gap-2">
              <HighlightBadge icon={<Star className="w-3.5 h-3.5" />}>
                aegtasgasdfgarg
              </HighlightBadge>
              <HighlightBadge icon={<Star className="w-3.5 h-3.5" />}>
                asrgasdgdg
              </HighlightBadge>
            </div>
          </div>
        </div>
      </div>
    </div>);

}
// --- Subcomponents ---
function Badge({
  children,
  type,
  icon




}: {children: React.ReactNode;type: 'green' | 'blue';icon?: React.ReactNode;}) {
  const styles = {
    green: 'bg-brand-green-bg text-brand-green-text',
    blue: 'bg-brand-blue-bg text-brand-blue-text'
  };
  return (
    <span
      className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold tracking-wide ${styles[type]}`}>
      
      {icon}
      {children}
    </span>);

}
function ActionButton({
  children,
  icon,
  primary,
  danger





}: {children: React.ReactNode;icon: React.ReactNode;primary?: boolean;danger?: boolean;}) {
  const base =
  'flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-colors border';
  let variant = 'border-gray-200 text-gray-700 hover:bg-gray-50';
  if (primary) {
    variant = 'bg-brown border-brown text-white hover:bg-brown-dark';
  } else if (danger) {
    variant = 'border-red-200 text-red-600 hover:bg-red-50';
  }
  return (
    <button className={`${base} ${variant}`}>
      {icon}
      {children}
    </button>);

}
function InfoBox({ label, value }: {label: string;value: string;}) {
  return (
    <div className="bg-gray-50 rounded-xl px-4 py-3 min-w-[120px]">
      <div className="text-xs text-gray-500 mb-1">{label}</div>
      <div className="font-semibold text-gray-900">{value}</div>
    </div>);

}
function MetaItem({
  icon,
  label,
  value




}: {icon: React.ReactNode;label: string;value: string;}) {
  return (
    <div className="flex items-start gap-3">
      <div className="text-gray-400 [&>svg]:w-5 [&>svg]:h-5 mt-0.5">{icon}</div>
      <div>
        <div className="text-xs text-gray-500 mb-0.5">{label}</div>
        <div className="text-sm font-medium text-gray-900">{value}</div>
      </div>
    </div>);

}
function AttributeCard({
  icon,
  label,
  value




}: {icon: React.ReactNode;label: string;value: string;}) {
  return (
    <div className="bg-white rounded-2xl p-4 shadow-sm border border-black/5 flex items-center gap-4">
      <div className="w-10 h-10 rounded-full bg-[#f5f2ec] text-brown flex items-center justify-center [&>svg]:w-5 [&>svg]:h-5">
        {icon}
      </div>
      <div>
        <div className="text-xs text-gray-500 font-medium mb-0.5">{label}</div>
        <div className="font-semibold text-gray-900">{value}</div>
      </div>
    </div>);

}
function ListItem({
  icon,
  label,
  value,
  copyable





}: {icon: React.ReactNode;label: string;value: string;copyable?: boolean;}) {
  return (
    <div className="flex items-center justify-between group">
      <div className="flex items-center gap-3">
        <div className="text-gray-400 [&>svg]:w-4 [&>svg]:h-4">{icon}</div>
        <span className="text-sm text-gray-600">{label}</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="text-sm font-medium text-gray-900">{value}</span>
        {copyable &&
        <button className="text-gray-400 hover:text-gray-600 opacity-0 group-hover:opacity-100 transition-opacity">
            <Copy className="w-4 h-4" />
          </button>
        }
      </div>
    </div>);

}
function ContentBox({
  icon,
  title,
  children




}: {icon: React.ReactNode;title: string;children: React.ReactNode;}) {
  return (
    <div className="bg-gray-50 rounded-xl p-4">
      <div className="flex items-center gap-2 mb-3">
        <div className="text-gray-400 [&>svg]:w-4 [&>svg]:h-4">{icon}</div>
        <h4 className="font-medium text-gray-900">{title}</h4>
      </div>
      {children}
    </div>);

}
function TagBadge({
  children,
  icon



}: {children: React.ReactNode;icon: React.ReactNode;}) {
  return (
    <span className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-gray-100 text-gray-600 text-sm font-medium">
      {icon}
      {children}
    </span>);

}
function HighlightBadge({
  children,
  icon



}: {children: React.ReactNode;icon: React.ReactNode;}) {
  return (
    <span className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-yellow-50 text-yellow-700 border border-yellow-200/50 text-sm font-medium">
      {icon}
      {children}
    </span>);

}